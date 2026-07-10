package com.library.service.impl;

import com.library.entity.Borrowing;
import com.library.entity.Book;
import com.library.entity.Reader;
import com.library.dto.BorrowingDTO;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.BookMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.BorrowingService;
import com.library.exception.BusinessException;
import com.library.exception.OptimisticLockException;
import com.library.service.ReaderLevelService;
import com.library.service.SystemConfigService;
import com.library.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowingServiceImpl implements BorrowingService {

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ReaderLevelService readerLevelService;

    @Autowired
    private BlacklistService blacklistService;

    @Override
    public List<BorrowingDTO> getAllBorrowings() {
        return borrowingMapper.findAllWithBook();
    }

    @Override
    public Borrowing getBorrowingById(Integer id) {
        return borrowingMapper.findById(id);
    }

    @Override
    public List<Borrowing> getBorrowingsByReaderId(Integer readerId) {
        return borrowingMapper.findByReaderId(readerId);
    }

    @Override
    public List<Borrowing> getBorrowingsByReaderIdString(String readerId) {
        return borrowingMapper.findByReaderIdString(readerId);
    }

    /**
     * 从SystemConfig表读取配置值，带默认值
     */
    private String getConfigValue(String key, String defaultValue) {
        String value = systemConfigService.getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    @Override
    @Transactional
    public Borrowing borrowBook(String readerIdString, Integer bookId) {
        // 通过业务readerId查找读者
        Reader reader = readerMapper.findByReaderId(readerIdString);
        if (reader == null) {
            throw new RuntimeException("读者不存在");
        }

        // 检查读者账号状态：status != 1 表示已禁用
        if (reader.getStatus() == null || reader.getStatus() != 1) {
            throw new RuntimeException("读者账号已被禁用，无法借书");
        }

        // 检查黑名单
        if (blacklistService.isBlacklisted(reader.getId())) {
            throw new BusinessException("您在黑名单中，无法借书");
        }

        // 检查图书是否存在
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 检查读者借阅数量限制
        if (reader.getCurrentBorrowCount() >= reader.getMaxBorrowCount()) {
            throw new RuntimeException("已达到最大借阅数量限制");
        }

        // 检查是否有未缴纳的罚款
        if (reader.getFineAmount() != null && reader.getFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("您有未缴纳的罚款（¥" + reader.getFineAmount() + "），请先缴清后再借书");
        }

        // 乐观锁：原子扣减可用数量，若返回0则说明库存不足（可能被并发借走）
        // WHERE available_count > 0 保证不会借出负数库存
        int affected = bookMapper.decrementAvailableCount(bookId);
        if (affected == 0) {
            // 重新查询最新库存，给用户更精确的提示
            Book latestBook = bookMapper.findById(bookId);
            if (latestBook.getAvailableCount() == 0) {
                throw new OptimisticLockException("图书已借完，请稍后重试");
            }
            // 理论上不会到这里，但以防万一
            throw new OptimisticLockException("库存不足，借书失败，请重试");
        }

        // 创建借阅记录
        int defaultDays = Integer.parseInt(getConfigValue("library.borrowing.default-days", "30"));
        Borrowing borrowing = new Borrowing();
        borrowing.setReaderId(reader.getId());
        borrowing.setBookId(bookId);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(defaultDays));
        borrowing.setRenewCount(0);
        borrowing.setStatus(1); // 1: 借阅中
        borrowingMapper.insert(borrowing);

        // 使用原子更新读者当前借阅数量
        readerMapper.incrementBorrowCount(reader.getId());

        // 初始化读者等级记录并加积分
        readerLevelService.initReaderLevel(reader.getId());
        readerLevelService.addPoints(reader.getId(), 10);
        readerLevelService.incrementBorrowCount(reader.getId());

        return borrowing;
    }

    @Override
    @Transactional
    public Borrowing returnBook(Integer borrowingId) {
        Borrowing borrowing = borrowingMapper.findById(borrowingId);
        if (borrowing == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (borrowing.getStatus() != 1) {
            throw new RuntimeException("该借阅记录不是借阅状态");
        }

        // 更新借阅记录
        borrowing.setReturnDate(LocalDateTime.now());

        // 计算逾期罚款（每天0.1元，宽限期内免罚）
        BigDecimal fineAmount = BigDecimal.ZERO;
        if (LocalDateTime.now().isAfter(borrowing.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
            // 罚款宽限期：逾期 N 天内免罚（默认3天，从 system_config 读取）
            int graceDays = Integer.parseInt(getConfigValue("library.fine.grace-days", "3"));
            if (overdueDays > graceDays) {
                double dailyRate = Double.parseDouble(getConfigValue("library.fine.daily-rate", "0.10"));
                fineAmount = new BigDecimal(overdueDays).multiply(BigDecimal.valueOf(dailyRate));
            }
            borrowing.setFineAmount(fineAmount);
        }

        borrowing.setStatus(2); // 2: 已归还
        borrowingMapper.update(borrowing);

        // 原子累加读者罚款金额（避免并发竞态）
        if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            readerMapper.incrementFineAmount(borrowing.getReaderId(), fineAmount);
        }

        // 原子增加图书可用数量
        bookMapper.incrementAvailableCount(borrowing.getBookId());

        // 使用原子更新读者当前借阅数量
        readerMapper.decrementBorrowCount(borrowing.getReaderId());

        // 根据是否逾期调整积分
        if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 逾期归还，扣积分
            readerLevelService.subtractPoints(borrowing.getReaderId(), 20);
        } else {
            // 按时归还，加积分
            readerLevelService.addPoints(borrowing.getReaderId(), 5);
            readerLevelService.incrementReturnOnTime(borrowing.getReaderId());
        }

        return borrowing;
    }

    @Override
    @Transactional
    public Borrowing renewBook(Integer borrowingId) {
        Borrowing borrowing = borrowingMapper.findById(borrowingId);
        if (borrowing == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        if (borrowing.getStatus() != 1) {
            throw new RuntimeException("该借阅记录不是借阅状态");
        }

        // 检查是否已续借过
        int maxRenewCount = Integer.parseInt(getConfigValue("library.borrowing.max-renew-count", "2"));
        if (borrowing.getRenewCount() >= maxRenewCount) {
            throw new RuntimeException("已达到最大续借次数");
        }

        // 检查是否已逾期，逾期的图书不能续借
        if (borrowing.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("图书已逾期，无法续借");
        }

        // 续借窗口限制：只能在到期前 X 天内操作（默认7天）
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), borrowing.getDueDate());
        int renewWindowDays = Integer.parseInt(getConfigValue("library.borrowing.renew-window-days", "7"));
        if (daysUntilDue > renewWindowDays) {
            throw new BusinessException("只能在到期前" + renewWindowDays + "天内续借");
        }

        // 更新借阅记录
        int renewDays = Integer.parseInt(getConfigValue("library.borrowing.renew-days", "30"));
        borrowing.setDueDate(borrowing.getDueDate().plusDays(renewDays));
        borrowing.setRenewCount(borrowing.getRenewCount() + 1);
        borrowingMapper.update(borrowing);

        return borrowing;
    }

    @Override
    @Transactional
    public void payFine(Integer borrowingId) {
        Borrowing borrowing = borrowingMapper.findById(borrowingId);
        if (borrowing == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        // 记录需要支付的罚款金额
        BigDecimal fineAmount = borrowing.getFineAmount();

        // 清除借阅记录的罚款
        borrowingMapper.payFine(borrowingId);

        // 原子减少读者的罚款金额
        if (fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            readerMapper.decrementFineAmount(borrowing.getReaderId(), fineAmount);
        }
    }

    @Override
    public void validateOwnership(Integer borrowingId, String readerId) {
        Borrowing borrowing = borrowingMapper.findById(borrowingId);
        if (borrowing == null) {
            throw new BusinessException("借阅记录不存在");
        }
        Reader reader = readerMapper.findById(borrowing.getReaderId());
        if (reader == null || !reader.getReaderId().equals(readerId)) {
            throw new BusinessException(403, "无权操作他人的借阅记录");
        }
    }
}

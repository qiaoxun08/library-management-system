package com.library.service;

import com.library.dto.BorrowingDTO;
import com.library.entity.Book;
import com.library.entity.Borrowing;
import com.library.entity.Reader;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.mapper.ReaderMapper;
import com.library.service.impl.BorrowingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowingService 单元测试")
class BorrowingServiceTest {

    @Mock
    private BorrowingMapper borrowingMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private ReaderMapper readerMapper;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private ReaderLevelService readerLevelService;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private RedisLockService redisLockService;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private Reader testReader;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testReader = new Reader();
        testReader.setId(1);
        testReader.setReaderId("2024001");
        testReader.setStatus(1);
        testReader.setCurrentBorrowCount(0);
        testReader.setMaxBorrowCount(5);
        testReader.setFineAmount(BigDecimal.ZERO);

        testBook = new Book();
        testBook.setId(1);
        testBook.setTitle("测试图书");
        testBook.setAvailableCount(3);

        // 默认配置值（lenient：部分测试可能不需要全部 stub）
        lenient().when(systemConfigService.getConfigValue("library.borrowing.default-days")).thenReturn("30");
        lenient().when(systemConfigService.getConfigValue("library.borrowing.renew-days")).thenReturn("30");
        lenient().when(systemConfigService.getConfigValue("library.borrowing.max-renew-count")).thenReturn("2");
        lenient().when(systemConfigService.getConfigValue("library.borrowing.renew-window-days")).thenReturn("7");
        lenient().when(systemConfigService.getConfigValue("library.fine.daily-rate")).thenReturn("0.10");
        lenient().when(systemConfigService.getConfigValue("library.fine.grace-days")).thenReturn("3");

        // 模拟分布式锁：直接执行传入的action，不真正加锁
        lenient().when(redisLockService.executeWithLock(anyString(), anyLong(), any(RedisLockService.LockAction.class)))
                .thenAnswer(invocation -> {
                    RedisLockService.LockAction<?> action = invocation.getArgument(2);
                    return action.execute();
                });
    }

    // ==================== 借书测试 ====================

    @Test
    @DisplayName("借书 - 正常借阅")
    void borrowBook_Success() {
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(bookMapper.findById(1)).thenReturn(testBook);
        when(bookMapper.decrementAvailableCount(1)).thenReturn(1);
        when(borrowingMapper.insert(any(Borrowing.class))).thenReturn(1);

        Borrowing result = borrowingService.borrowBook("2024001", 1);

        assertNotNull(result);
        assertEquals(1, result.getReaderId());
        assertEquals(1, result.getBookId());
        assertEquals(1, result.getStatus());
        assertEquals(0, result.getRenewCount());
        assertNotNull(result.getBorrowDate());
        assertNotNull(result.getDueDate());

        verify(bookMapper).decrementAvailableCount(1);
        verify(readerMapper).incrementBorrowCount(1);
        verify(readerLevelService).initReaderLevel(1);
        verify(readerLevelService).addPoints(1, 10);
    }

    @Test
    @DisplayName("借书 - 读者不存在")
    void borrowBook_ReaderNotFound() {
        when(readerMapper.findByReaderId("999999")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.borrowBook("999999", 1));
        assertEquals("读者不存在", ex.getMessage());
    }

    @Test
    @DisplayName("借书 - 读者已禁用")
    void borrowBook_ReaderDisabled() {
        testReader.setStatus(0);
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.borrowBook("2024001", 1));
        assertEquals("读者账号已被禁用，无法借书", ex.getMessage());
    }

    @Test
    @DisplayName("借书 - 在黑名单中")
    void borrowBook_Blacklisted() {
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowingService.borrowBook("2024001", 1));
        assertTrue(ex.getMessage().contains("黑名单"));
    }

    @Test
    @DisplayName("借书 - 图书不存在")
    void borrowBook_BookNotFound() {
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(bookMapper.findById(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.borrowBook("2024001", 999));
        assertEquals("图书不存在", ex.getMessage());
    }

    @Test
    @DisplayName("借书 - 超过最大借阅数量")
    void borrowBook_MaxBorrowLimit() {
        testReader.setCurrentBorrowCount(5);
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(bookMapper.findById(1)).thenReturn(testBook);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.borrowBook("2024001", 1));
        assertEquals("已达到最大借阅数量限制", ex.getMessage());
    }

    @Test
    @DisplayName("借书 - 有未缴纳罚款")
    void borrowBook_OutstandingFine() {
        testReader.setFineAmount(new BigDecimal("5.00"));
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(bookMapper.findById(1)).thenReturn(testBook);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowingService.borrowBook("2024001", 1));
        assertTrue(ex.getMessage().contains("罚款"));
    }

    @Test
    @DisplayName("借书 - 图书已借完")
    void borrowBook_BookOutOfStock() {
        when(readerMapper.findByReaderId("2024001")).thenReturn(testReader);
        when(blacklistService.isBlacklisted(1)).thenReturn(false);
        when(bookMapper.findById(1)).thenReturn(testBook);
        when(bookMapper.decrementAvailableCount(1)).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.borrowBook("2024001", 1));
        assertTrue(ex.getMessage().contains("库存不足") || ex.getMessage().contains("借完"));
    }

    // ==================== 还书测试 ====================

    @Test
    @DisplayName("还书 - 正常归还（无逾期）")
    void returnBook_Success_NoOverdue() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setRenewCount(0);
        borrowing.setDueDate(LocalDateTime.now().plusDays(10));

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBook(1);

        assertEquals(2, result.getStatus());
        assertNotNull(result.getReturnDate());
        // 非逾期还书时 fineAmount 为 null 或 ZERO
        assertTrue(result.getFineAmount() == null || result.getFineAmount().compareTo(BigDecimal.ZERO) == 0);
        verify(bookMapper).incrementAvailableCount(1);
        verify(readerMapper).decrementBorrowCount(1);
        verify(readerLevelService).addPoints(1, 5);
    }

    @Test
    @DisplayName("还书 - 逾期归还（超过宽限期，需罚款）")
    void returnBook_Overdue_WithFine() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setDueDate(LocalDateTime.now().minusDays(10)); // 逾期10天

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBook(1);

        assertEquals(2, result.getStatus());
        // 逾期10天 - 3天宽限期 = 7天罚款，每天0.1元 = 0.70
        assertTrue(result.getFineAmount().compareTo(BigDecimal.ZERO) > 0);
        verify(readerLevelService).subtractPoints(1, 20);
    }

    @Test
    @DisplayName("还书 - 逾期但在宽限期内（免罚）")
    void returnBook_Overdue_WithinGracePeriod() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setDueDate(LocalDateTime.now().minusDays(2)); // 逾期2天，在3天宽限期内

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBook(1);

        assertEquals(BigDecimal.ZERO, result.getFineAmount());
        verify(readerLevelService).addPoints(1, 5); // 按时归还加积分
    }

    @Test
    @DisplayName("还书 - 借阅记录不存在")
    void returnBook_NotFound() {
        when(borrowingMapper.findById(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.returnBook(999));
        assertEquals("借阅记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("还书 - 非借阅状态")
    void returnBook_WrongStatus() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setStatus(2); // 已归还

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.returnBook(1));
        assertEquals("该借阅记录不是借阅状态", ex.getMessage());
    }

    // ==================== 续借测试 ====================

    @Test
    @DisplayName("续借 - 正常续借（到期前7天内）")
    void renewBook_Success() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setRenewCount(0);
        borrowing.setDueDate(LocalDateTime.now().plusDays(5)); // 到期前5天，在7天窗口内

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        Borrowing result = borrowingService.renewBook(1);

        assertEquals(1, result.getRenewCount());
        verify(borrowingMapper).update(borrowing);
    }

    @Test
    @DisplayName("续借 - 超过最大续借次数")
    void renewBook_MaxRenewCount() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setRenewCount(2); // 已达最大次数
        borrowing.setDueDate(LocalDateTime.now().plusDays(5));

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.renewBook(1));
        assertEquals("已达到最大续借次数", ex.getMessage());
    }

    @Test
    @DisplayName("续借 - 已逾期不能续借")
    void renewBook_Overdue() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setRenewCount(0);
        borrowing.setDueDate(LocalDateTime.now().minusDays(1)); // 已逾期

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.renewBook(1));
        assertEquals("图书已逾期，无法续借", ex.getMessage());
    }

    @Test
    @DisplayName("续借 - 不在续借窗口期内（到期前超过7天）")
    void renewBook_OutsideWindow() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setBookId(1);
        borrowing.setStatus(1);
        borrowing.setRenewCount(0);
        borrowing.setDueDate(LocalDateTime.now().plusDays(15)); // 到期前15天，超出7天窗口

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowingService.renewBook(1));
        assertTrue(ex.getMessage().contains("续借"));
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("查询所有借阅记录")
    void getAllBorrowings() {
        when(borrowingMapper.findAllWithBook()).thenReturn(Collections.emptyList());

        List<BorrowingDTO> result = borrowingService.getAllBorrowings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(borrowingMapper).findAllWithBook();
    }

    @Test
    @DisplayName("按读者ID查询借阅记录")
    void getBorrowingsByReaderId() {
        when(borrowingMapper.findByReaderId(1)).thenReturn(Collections.emptyList());

        List<Borrowing> result = borrowingService.getBorrowingsByReaderId(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 缴纳罚款测试 ====================

    @Test
    @DisplayName("缴纳罚款 - 正常缴纳")
    void payFine_Success() {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(1);
        borrowing.setReaderId(1);
        borrowing.setFineAmount(new BigDecimal("5.00"));

        when(borrowingMapper.findById(1)).thenReturn(borrowing);

        borrowingService.payFine(1);

        verify(borrowingMapper).payFine(1);
        verify(readerMapper).decrementFineAmount(1, new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("缴纳罚款 - 借阅记录不存在")
    void payFine_NotFound() {
        when(borrowingMapper.findById(999)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowingService.payFine(999));
        assertEquals("借阅记录不存在", ex.getMessage());
    }
}

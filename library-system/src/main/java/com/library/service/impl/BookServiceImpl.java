package com.library.service.impl;

import com.library.entity.Book;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BorrowingMapper borrowingMapper;

    @Override
    public List<Book> getAllBooks() {
        return bookMapper.findAll();
    }

    @Override
    public List<Book> getAllBooksWithStatus(Integer status) {
        return bookMapper.findAllWithStatus(status);
    }

    @Override
    public Book getBookById(Integer id) {
        return bookMapper.findById(id);
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        return bookMapper.findByKeyword(keyword);
    }

    @Override
    public List<Book> advancedSearch(String keyword, String category, String publisher, Integer year, String isbn, String status) {
        return bookMapper.advancedSearch(keyword, category, publisher, year, isbn, status);
    }

    @Override
    public Book addBook(Book book) {
        // ISBN格式校验
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            if (!isValidIsbn13(book.getIsbn())) {
                throw new BusinessException("ISBN格式无效（需13位数字，最后一位为校验码）: " + book.getIsbn());
            }
            // ISBN唯一性检查
            Book existingBook = bookMapper.findByIsbn(book.getIsbn());
            if (existingBook != null) {
                throw new BusinessException("ISBN已存在: " + book.getIsbn());
            }
        }
        if (book.getAvailableCount() == null && book.getTotalCount() != null) {
            book.setAvailableCount(book.getTotalCount());
        }
        bookMapper.insert(book);
        return book;
    }

    @Override
    public Book updateBook(Book book) {
        // 检查图书是否存在
        Book existingBook = bookMapper.findById(book.getId());
        if (existingBook == null) {
            throw new RuntimeException("图书不存在");
        }
        bookMapper.update(book);
        return book;
    }

    @Override
    public void deleteBook(Integer id) {
        // 检查是否有未归还的借阅记录
        int activeBorrowings = borrowingMapper.countActiveByBookId(id);
        if (activeBorrowings > 0) {
            throw new RuntimeException("该图书仍有未归还的借阅记录，无法删除");
        }
        bookMapper.delete(id);
    }

    @Override
    @Transactional
    public void importBooks(List<Book> books) {
        for (Book book : books) {
            // ISBN格式校验（导入时跳过无效ISBN，不阻断整个导入）
            if (book.getIsbn() != null && !book.getIsbn().isEmpty() && !isValidIsbn13(book.getIsbn())) {
                log.warn("导入图书ISBN格式无效，已跳过: {}", book.getIsbn());
                continue;
            }
            // ISBN重复检查
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                Book existing = bookMapper.findByIsbn(book.getIsbn());
                if (existing != null) {
                    log.warn("导入图书ISBN已存在，已跳过: {}", book.getIsbn());
                    continue;
                }
            }
            bookMapper.insert(book);
        }
    }

    @Override
    public List<Book> exportBooks() {
        return bookMapper.findAll();
    }

    /**
     * ISBN-13 格式校验（13位数字，最后一位为校验码）
     */
    private boolean isValidIsbn13(String isbn) {
        if (isbn == null) return false;
        isbn = isbn.replaceAll("-", "");
        if (isbn.length() != 13 || !isbn.matches("\\d{13}")) return false;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (isbn.charAt(12) - '0');
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的图书");
        }
        bookMapper.batchUpdateStatus(ids, status);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的图书");
        }
        // 检查是否有未归还的借阅记录
        for (Integer id : ids) {
            int activeBorrowings = borrowingMapper.countActiveByBookId(id);
            if (activeBorrowings > 0) {
                Book book = bookMapper.findById(id);
                String title = book != null ? book.getTitle() : "ID:" + id;
                throw new BusinessException("图书「" + title + "」仍有未归还的借阅记录，无法删除");
            }
        }
        bookMapper.batchDelete(ids);
    }
}

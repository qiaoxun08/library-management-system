package com.library.service.impl;

import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowingMapper;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

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
    public Book addBook(Book book) {
        // ISBN唯一性检查
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            Book existingBook = bookMapper.findByIsbn(book.getIsbn());
            if (existingBook != null) {
                throw new RuntimeException("ISBN已存在: " + book.getIsbn());
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
            bookMapper.insert(book);
        }
    }

    @Override
    public List<Book> exportBooks() {
        return bookMapper.findAll();
    }
}

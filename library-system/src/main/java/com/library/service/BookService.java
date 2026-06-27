package com.library.service;

import com.library.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> getAllBooks();
    List<Book> getAllBooksWithStatus(Integer status);
    Book getBookById(Integer id);
    List<Book> searchBooks(String keyword);
    Book addBook(Book book);
    Book updateBook(Book book);
    void deleteBook(Integer id);
    void importBooks(List<Book> books);
    List<Book> exportBooks();
}

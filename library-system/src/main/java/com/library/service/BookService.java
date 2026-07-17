package com.library.service;

import com.library.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> getAllBooks();
    List<Book> getAllBooksWithStatus(Integer status);
    Book getBookById(Integer id);
    List<Book> searchBooks(String keyword);
    List<Book> advancedSearch(String keyword, String category, String publisher, Integer year, String isbn, String status);
    Book addBook(Book book);
    Book updateBook(Book book);
    void deleteBook(Integer id);
    void importBooks(List<Book> books);
    List<Book> exportBooks();

    /** 批量更新图书状态 */
    void batchUpdateStatus(List<Integer> ids, Integer status);

    /** 批量删除图书 */
    void batchDelete(List<Integer> ids);
}

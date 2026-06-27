package com.library.mapper;

import com.library.dto.BookCategoryCount;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    Book findById(@Param("id") Integer id);
    Book findByIsbn(@Param("isbn") String isbn);
    List<Book> findAll();
    List<Book> findAllWithStatus(@Param("status") Integer status);
    List<Book> findByTitle(@Param("title") String title);
    List<Book> findByKeyword(@Param("keyword") String keyword);
    List<Book> findByCategory(@Param("category") String category);
    int insert(Book book);
    int update(Book book);
    int delete(@Param("id") Integer id);
    int updateAvailableCount(@Param("id") Integer id, @Param("availableCount") Integer availableCount);
    int decrementAvailableCount(@Param("id") Integer id);
    int incrementAvailableCount(@Param("id") Integer id);
    List<BookCategoryCount> countByCategory();
    int countTotalBooks();
    int countAvailableBooks();
    List<Book> findAvailableByCategory(@Param("category") String category);
    List<Book> findPopularBooks(@Param("limit") int limit);
    List<Book> findAvailableByCategories(@Param("categories") List<String> categories, @Param("excludeBookIds") List<Integer> excludeBookIds);
    List<Book> findSimilarByCategory(@Param("category") String category, @Param("excludeIds") List<Integer> excludeIds, @Param("limit") int limit);
}

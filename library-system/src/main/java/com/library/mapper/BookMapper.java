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

    // 高级搜索：支持关键词、分类、出版社、出版年份、ISBN、库存状态组合查询
    List<Book> advancedSearch(@Param("keyword") String keyword, @Param("category") String category,
                              @Param("publisher") String publisher, @Param("year") Integer year,
                              @Param("isbn") String isbn, @Param("status") String status);

    /** 热门图书TOP10（按借阅次数） */
    List<java.util.Map<String, Object>> findHotBooksTop10();

    /** 批量更新图书状态 */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);

    /** 批量删除图书 */
    int batchDelete(@Param("ids") List<Integer> ids);
}

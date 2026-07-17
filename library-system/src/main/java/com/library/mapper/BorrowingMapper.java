package com.library.mapper;

import com.library.entity.Book;
import com.library.entity.Borrowing;
import com.library.dto.BorrowingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BorrowingMapper {
    Borrowing findById(@Param("id") Integer id);
    List<Borrowing> findAll();
    List<BorrowingDTO> findAllWithBook();
    List<Borrowing> findByReaderId(@Param("readerId") Integer readerId);
    List<Borrowing> findByReaderIdString(@Param("readerId") String readerId);
    List<BorrowingDTO> findByReaderIdWithBook(@Param("readerId") String readerId);
    List<Borrowing> findByBookId(@Param("bookId") Integer bookId);
    List<Borrowing> findByStatus(@Param("status") Integer status);
    int insert(Borrowing borrowing);
    int update(Borrowing borrowing);
    int delete(@Param("id") Integer id);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int updateReturnDate(@Param("id") Integer id, @Param("returnDate") java.time.LocalDateTime returnDate);
    int countTodayBorrowings();
    int countActiveBorrowings();
    int countOverdueBooks();
    List<Integer> countByMonth();
    int countActiveByBookId(@Param("bookId") Integer bookId);
    int payFine(@Param("id") Integer id);
    Borrowing findActiveByReaderAndBook(@Param("readerId") Integer readerId, @Param("bookId") Integer bookId);
    List<Borrowing> findDueSoonOrOverdue(@Param("remindDays") int remindDays);
    List<Integer> findBorrowedBookIdsByReaderId(@Param("readerId") Integer readerId);
    List<java.util.Map<String, Object>> findCategoryCountsByReaderId(@Param("readerId") Integer readerId);
    int countOverdueByReaderId(@Param("readerId") Integer readerId);
    List<Borrowing> findOverdueByReaderId(@Param("readerId") Integer readerId);
    List<Borrowing> findActiveByReaderId(@Param("readerId") Integer readerId);
    int countByReaderIdAndStatus(@Param("readerId") Integer readerId, @Param("status") Integer status);
    List<java.util.Map<String, Object>> countByCategoryAndDays(@Param("days") int days);
    List<Book> findPopularBooks(@Param("limit") int limit);
    List<Borrowing> findRecentByReaderId(@Param("readerId") String readerId, @Param("days") int days);

    /** 按月统计逾期率（最近12个月） */
    List<java.util.Map<String, Object>> countOverdueRateByMonth();

    /** 按院系统计借阅量 */
    List<java.util.Map<String, Object>> countByDepartment();

    /** 查询读者的违约明细 */
    List<java.util.Map<String, Object>> findViolationDetails(@Param("readerId") Integer readerId);

    /** 今日借阅量按小时分布 */
    List<java.util.Map<String, Object>> countTodayByHour();

    /** 借阅分类分布 */
    List<java.util.Map<String, Object>> countByCategoryDistribution();

    /** 本月逾期率趋势（按天） */
    List<java.util.Map<String, Object>> countOverdueTrendByDay();
}

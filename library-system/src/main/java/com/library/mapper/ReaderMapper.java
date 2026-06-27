package com.library.mapper;

import com.library.entity.Reader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReaderMapper {
    Reader findById(@Param("id") Integer id);
    Reader findByReaderId(@Param("readerId") String readerId);
    Reader findByUsername(@Param("username") String username);
    List<Reader> findAll();
    int insert(Reader reader);
    int update(Reader reader);
    int delete(@Param("id") Integer id);
    int countTotalReaders();
    int countActiveReaders();
    int incrementBorrowCount(@Param("id") Integer id);
    int decrementBorrowCount(@Param("id") Integer id);
    int incrementFineAmount(@Param("id") Integer id, @Param("amount") java.math.BigDecimal amount);
    int decrementFineAmount(@Param("id") Integer id, @Param("amount") java.math.BigDecimal amount);
    int updateStatus(@Param("id") Integer id, @Param("status") int status);
    int updatePreferredCategories(@Param("id") Integer id, @Param("preferredCategories") String preferredCategories);
    int updateLanguage(@Param("id") Integer id, @Param("language") String language);
}

package com.library.mapper;

import com.library.entity.Librarian;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LibrarianMapper {
    Librarian findByUsername(@Param("username") String username);
    List<Librarian> findAll();
    int insert(Librarian librarian);
    int update(Librarian librarian);
    int delete(@Param("id") Integer id);
}

package com.library.mapper;

import com.library.entity.StudyBuddy;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 学习搭档Mapper
 */
@Mapper
public interface StudyBuddyMapper {
    int insert(StudyBuddy buddy);
    int update(StudyBuddy buddy);
    StudyBuddy findByReaderId(Integer readerId);
    List<StudyBuddy> findOpenBuddies();
    int deleteByReaderId(Integer readerId);
}
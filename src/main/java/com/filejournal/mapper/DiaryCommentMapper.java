package com.filejournal.mapper;

import com.filejournal.model.DiaryComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DiaryCommentMapper {
    int insert(DiaryComment comment);
    List<DiaryComment> selectByDiaryId(@Param("diaryId") Integer diaryId);

    int deleteById(Integer id);
    DiaryComment selectById(Integer id);
}

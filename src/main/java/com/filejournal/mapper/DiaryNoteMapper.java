package com.filejournal.mapper;

import com.filejournal.model.DiaryNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DiaryNoteMapper {
    int insert(DiaryNote note);
    List<DiaryNote> selectByDiaryId(@Param("diaryId") Integer diaryId);
}
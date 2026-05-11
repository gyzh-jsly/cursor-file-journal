package com.filejournal.service;

import com.filejournal.model.Diary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DiaryService {
    Diary createDiary(String content, Boolean isPinned);
    Diary updateDiary(Integer id, String content, Boolean isPinned);
    Diary getDiary(Integer id);
    List<Diary> getDiariesByDate(LocalDate date, String sort);
    List<Map<String, Object>> getCalendarData(Integer year, Integer month);
    Map<String, Integer> getYearStats(Integer year);
    boolean canEdit(Diary diary);
    LocalDateTime calculateDeadline(LocalDateTime createdAt);

    List<Diary> getDiariesByBatchId(Integer batchId);
    void deleteBatchDiaries(Integer batchId);

    void importDiaries(String Filename, String content);
}
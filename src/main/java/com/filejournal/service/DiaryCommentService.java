package com.filejournal.service;

import com.filejournal.model.DiaryComment;
import java.util.List;

public interface DiaryCommentService {
    DiaryComment addComment(Integer diaryId, String selectedText,
                            Integer startOffset, String content);
    List<DiaryComment> getCommentsByDiaryId(Integer diaryId);

    void deleteComment(Integer id);
}
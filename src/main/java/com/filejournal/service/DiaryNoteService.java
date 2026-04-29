package com.filejournal.service;

import com.filejournal.model.DiaryNote;
import java.util.List;

public interface DiaryNoteService {
    DiaryNote addNote(Integer diaryId, String content);
    List<DiaryNote> getNotesByDiaryId(Integer diaryId);
}
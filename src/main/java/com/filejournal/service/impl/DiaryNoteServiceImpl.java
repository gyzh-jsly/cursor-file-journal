package com.filejournal.service.impl;

import com.filejournal.mapper.DiaryNoteMapper;
import com.filejournal.model.DiaryNote;
import com.filejournal.service.DiaryNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiaryNoteServiceImpl implements DiaryNoteService {

    @Autowired
    private DiaryNoteMapper noteMapper;

    @Override
    public DiaryNote addNote(Integer diaryId, String content) {
        DiaryNote note = new DiaryNote();
        note.setDiaryId(diaryId);
        note.setNote(content);
        note.setCreatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    @Override
    public List<DiaryNote> getNotesByDiaryId(Integer diaryId) {
        return noteMapper.selectByDiaryId(diaryId);
    }
}
package com.filejournal.service.impl;

import com.filejournal.mapper.DiaryCommentMapper;
import com.filejournal.mapper.DiaryMapper;
import com.filejournal.model.Diary;
import com.filejournal.model.DiaryComment;
import com.filejournal.service.DiaryCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiaryCommentServiceImpl implements DiaryCommentService {

    @Autowired
    private DiaryCommentMapper commentMapper;
    @Autowired
    private DiaryMapper diaryMapper;

    @Override
    public DiaryComment addComment(Integer diaryId, String selectedText,
                                   Integer startOffset, String content) {
        //1. 查询日记当前版本
        Diary diary = diaryMapper.selectById(diaryId);
        if(diary==null){
            throw new IllegalArgumentException("日记不存在");
        }

        // 2. 校验 startOffset 是否有效
        String fullContent = diary.getContent();
        if (startOffset < 0 || startOffset >= fullContent.length()) {
            throw new IllegalArgumentException("选中的文字位置无效");
        }
        
        // 3. 校验 selectedText 是否与原文匹配（可选，增加数据完整性）
        String actualSelected = fullContent.substring(startOffset, 
                              Math.min(startOffset + selectedText.length(), fullContent.length()));
        if (!actualSelected.equals(selectedText)) {
            throw new IllegalArgumentException("选中的文字与原文不匹配");
        }
                                    
        DiaryComment comment = new DiaryComment();
        comment.setDiaryId(diaryId);
        comment.setVersion(diary.getModifyCount());
        comment.setSelectedText(selectedText);
        comment.setStartOffset(startOffset);
        comment.setComment(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public List<DiaryComment> getCommentsByDiaryId(Integer diaryId) {
        return commentMapper.selectByDiaryId(diaryId);
    }
}
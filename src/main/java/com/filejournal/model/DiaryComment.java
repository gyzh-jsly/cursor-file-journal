package com.filejournal.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DiaryComment {
    private Integer id;
    private Integer diaryId;
    private Integer version;
    private String selectedText;
    private Integer startOffset;
    private String comment;
    private LocalDateTime createdAt;

}

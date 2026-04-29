package com.filejournal.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DiaryNote {
    private Integer id;
    private Integer diaryId;
    private String note;
    private LocalDateTime createdAt;

}

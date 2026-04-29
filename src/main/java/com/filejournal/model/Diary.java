package com.filejournal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Diary {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editDeadline;
    private LocalDateTime lastModifiedAt;
    private Integer modifyCount;
    private String firstWeather;
    private Integer isPinned;
    private LocalDate diaryDate;

}

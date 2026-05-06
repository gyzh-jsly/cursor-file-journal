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
    //预览字段，不存数据库，仅用于展示
    private String contentPreview;

    //扩展字段3个
    private String sourceType;
    private Integer importBatchId;
    private Integer isFrozen;


    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }
}

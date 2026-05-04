package com.filejournal.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DossierFile {
    private Integer id;
    private Integer dirId;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String sourceType;   // WATCHED / MANUAL
    private String status;       // ACTIVE / ARCHIVED / LOCAL_DELETED
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
    // getter/setter 省略
}
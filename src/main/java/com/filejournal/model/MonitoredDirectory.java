package com.filejournal.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitoredDirectory {
    private Integer id;
    private String dirPath;
    private String dirName;
    private String status;       // ACTIVE / ARCHIVED / LOST
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
    // getter/setter 省略，请用 IDE 生成
}
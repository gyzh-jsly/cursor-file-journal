package com.filejournal.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileRevision {
    private Integer id;
    private Integer fileId;
    private Integer versionNumber;
    private LocalDateTime recordedAt;
    private Long fileSize;
    // getter/setter 省略
}
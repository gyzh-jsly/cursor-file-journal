package com.filejournal.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileNote {
    private Integer id;
    private Integer fileId;
    private Integer revisionId;   // NULL = 文件级备注
    private String content;
    private LocalDateTime createdAt;
    // getter/setter 省略
}
package com.filejournal.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImportBatch {
    private Integer id;
    private String batchName;
    private  String fileName;
    private  Integer importCount;
    private LocalDateTime createdAt;
}

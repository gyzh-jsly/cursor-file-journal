package com.filejournal.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommitLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long fileId;
    private LocalDateTime commitTime;
    private String message;
}

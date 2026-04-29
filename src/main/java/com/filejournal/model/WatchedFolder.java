package com.filejournal.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class WatchedFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String folderPath;
    private String folderName;
    private LocalDateTime createdAt;
}

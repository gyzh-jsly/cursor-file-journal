package com.filejournal.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class WatchedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long folderId;
    private String filePath;
    private String fileName;
    private LocalDateTime firstSeenTime;
    private String firstWeather;
    private Integer commitCount;
    private LocalDateTime lastCommitTime;
    private String lastMessage;
    private LocalDateTime createdAt;
}

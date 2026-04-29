package com.filejournal.model;

import java.time.LocalDateTime;

public class WatchedFileWithLastCommit {
    private Integer id;
    private Long folderId;
    private String filePath;
    private String fileName;
    private LocalDateTime firstSeenTime;
    private String firstWeather;
    private Integer commitCount;
    private LocalDateTime lastCommitTime;
    private String lastMessage;
    private LocalDateTime createdAt;

    // getter 和 setter 必须存在（篇幅原因省略，让 Cursor 生成或手动添加）
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Long getFolderId() {
        return folderId;
    }
    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public LocalDateTime getFirstSeenTime() {
        return firstSeenTime;
    }
    public void setFirstSeenTime(LocalDateTime firstSeenTime) {
        this.firstSeenTime = firstSeenTime;
    }
    public String getFirstWeather() {
        return firstWeather;
    }
    public void setFirstWeather(String firstWeather) {
        this.firstWeather = firstWeather;
    }
    public Integer getCommitCount() {
        return commitCount;
    }
    public void setCommitCount(Integer commitCount) {
        this.commitCount = commitCount;
    }
    public LocalDateTime getLastCommitTime() {
        return lastCommitTime;
    }
    public void setLastCommitTime(LocalDateTime lastCommitTime) {
        this.lastCommitTime = lastCommitTime;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
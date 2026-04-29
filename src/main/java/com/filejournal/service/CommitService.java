package com.filejournal.service;

import java.util.List;

import com.filejournal.model.CommitLog;

public interface CommitService {
    /**
     * 记录一次文件修改
     * @param fileId 文件ID（数据库主键）
     * @param message 备注信息
     */
    void commit(Long fileId, String message);

    /**5
     * 获取指定文件的完整 Commit 历史
     */
    List<CommitLog> getCommitHistory(Long fileId);

}

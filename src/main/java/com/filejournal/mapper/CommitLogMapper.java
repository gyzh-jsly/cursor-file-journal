package com.filejournal.mapper;

import com.filejournal.model.CommitLog;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface CommitLogMapper {

    int insert(CommitLog commitLog);

    List<CommitLog> selectAll();

    CommitLog selectById(Long id);

    int deleteById(Long id);

    /**5
     * 根据文件 ID 查询所有 Commit 记录，按时间倒序
     */
    List<CommitLog> selectByFileId(@Param("fileId") Long fileId);
}

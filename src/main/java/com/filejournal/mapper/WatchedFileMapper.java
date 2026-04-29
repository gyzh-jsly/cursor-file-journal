package com.filejournal.mapper;

import com.filejournal.model.WatchedFile;
import com.filejournal.model.WatchedFileWithLastCommit;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface WatchedFileMapper {

    int insert(WatchedFile watchedFile);

    List<WatchedFile> selectAll();

    /**
     * 查询指定文件夹下所有文件的最新活动（用于聚合流）
     * @param folderId 文件夹ID
     * @return 文件列表，包含最后一次 commit 信息
     */
    List<WatchedFileWithLastCommit> selectFilesWithLastCommit(@Param("folderId") Long folderId);

    WatchedFile selectById(Long id);
    WatchedFile selectByFilePath(String filePath);

    int deleteById(Long id);

    int countByFolderId(@Param("folderId") Long folderId);

    //第四步
    int update(WatchedFile file);
}

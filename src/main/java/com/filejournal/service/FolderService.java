package com.filejournal.service;

import com.filejournal.model.WatchedFileWithLastCommit;
import com.filejournal.model.WatchedFolder;
import java.util.List;

public interface FolderService {
    /**
     * 添加一个被监视的文件夹
     * @param folderPath 文件夹绝对路径
     * @param folderName 文件夹别名（可选）
     * @return 添加成功的文件夹对象
     */
    WatchedFolder addFolder(String folderPath, String folderName);
    
    /**
     * 获取所有被监视的文件夹
     */
    List<WatchedFolder> getAllFolders();

    /**
     * 获取指定文件夹下所有文件的最新活动/获取指定文件夹的聚合流数据
     * @param folderId 文件夹ID
     * @return 文件列表，包含最后一次 commit 信息
     */
    List<WatchedFileWithLastCommit> getFilesWithLastCommit(Long folderId);

    /**
     * 获取指定文件夹的聚合流数据
     */
    List<WatchedFileWithLastCommit> getFolderAggregate(Long folderId);
    
    /**
     * 根据 ID 删除监视文件夹
     */
    void deleteFolder(Long id);

    /**
     * 指定监视文件夹下的文件数量
     */
    int countFilesInFolder(Long folderId);

}

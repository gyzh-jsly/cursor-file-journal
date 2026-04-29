package com.filejournal.service.impl;

import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.mapper.WatchedFolderMapper;
import com.filejournal.model.WatchedFile;
import com.filejournal.model.WatchedFileWithLastCommit;
import com.filejournal.model.WatchedFolder;
import com.filejournal.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private WatchedFolderMapper folderMapper;    
    @Autowired
    private WatchedFileMapper fileMapper;


    @Override
    @Transactional
    public WatchedFolder addFolder(String folderPath, String folderName) {
        // 1. 校验文件夹是否存在
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("文件夹路径不存在或不是有效目录: " + folderPath);
        }
        
        // 2. 保存文件夹信息
        WatchedFolder watchedFolder = new WatchedFolder();
        watchedFolder.setFolderPath(folderPath);
        watchedFolder.setFolderName(folderName != null ? folderName : folder.getName());
        watchedFolder.setCreatedAt(LocalDateTime.now());
        folderMapper.insert(watchedFolder);
        
        // 3. 扫描文件夹下的文件，纳入监视
        scanAndAddFiles(folder, watchedFolder.getId());
        
        return watchedFolder;
    }
    
    @Override
    public List<WatchedFileWithLastCommit> getFilesWithLastCommit(Long folderId) {
        return fileMapper.selectFilesWithLastCommit(folderId);
    }


    @Override
    public List<WatchedFileWithLastCommit> getFolderAggregate(Long folderId) {
        return fileMapper.selectFilesWithLastCommit(folderId);
    }

    /**
     * 扫描文件夹，将文件加入 watched_file 表
     */
    private void scanAndAddFiles(File folder, Long folderId) {
        File[] files = folder.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isFile()) {
                // 检查是否已存在于数据库（按文件路径去重）
                WatchedFile existing = fileMapper.selectByFilePath(file.getAbsolutePath());
                if (existing == null) {
                    WatchedFile watchedFile = new WatchedFile();
                    watchedFile.setFolderId(folderId);
                    watchedFile.setFilePath(file.getAbsolutePath());
                    watchedFile.setFileName(file.getName());
                    watchedFile.setCreatedAt(LocalDateTime.now());
                    // 初次纳入管理，不记录 first_seen_time 和 first_weather
                    // 这些信息在用户第一次 Commit 时才记录
                    fileMapper.insert(watchedFile);
                }
            }
        }
    }

    @Override
    public List<WatchedFolder> getAllFolders() {
        return folderMapper.selectAll();
    }

    @Override
    public void deleteFolder(Long id) {
        folderMapper.deleteById(id);
    }

    @Override
    public int countFilesInFolder(Long folderId) {
        return fileMapper.countByFolderId(folderId);
    }

}
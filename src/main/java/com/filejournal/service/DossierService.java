package com.filejournal.service;

import com.filejournal.model.*;
import java.util.List;

public interface DossierService {
    // 目录管理
    MonitoredDirectory addDirectory(String dirPath, String dirName);
    List<MonitoredDirectory> getAllDirectories();
    void stopMonitoring(Integer dirId, String action);  // ARCHIVE / DELETE

    // 文件管理
    DossierFile addManualFile(String filePath);
    List<DossierFile> getFilesByDirectory(Integer dirId);
    List<DossierFile> getManualFiles();
    void archiveFile(Integer fileId);
    void deleteFile(Integer fileId);

    // 修订记录
    FileRevision recordRevision(Integer fileId, Long fileSize);
    List<FileRevision> getRevisions(Integer fileId);

    // 备注
    FileNote addNote(Integer fileId, Integer revisionId, String content);
    List<FileNote> getNotesByFile(Integer fileId);
    List<FileNote> getNotesByRevision(Integer revisionId);
    List<FileNote> getFileLevelNotes(Integer fileId);
}
package com.filejournal.service.impl;

import com.filejournal.mapper.*;
import com.filejournal.model.*;
import com.filejournal.service.DossierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DossierServiceImpl implements DossierService {

    @Autowired
    private MonitoredDirectoryMapper dirMapper;
    @Autowired
    private DossierFileMapper fileMapper;
    @Autowired
    private FileRevisionMapper revisionMapper;
    @Autowired
    private FileNoteMapper noteMapper;

    private static final int MERGE_WINDOW_MINUTES = 45;

    // ========== 目录管理 ==========

    @Override
    @Transactional
    public MonitoredDirectory addDirectory(String dirPath, String dirName) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("路径无效: " + dirPath);
        }

        MonitoredDirectory md = new MonitoredDirectory();
        md.setDirPath(dirPath);
        md.setDirName(dirName != null ? dirName : dir.getName());
        md.setStatus("ACTIVE");
        md.setCreatedAt(LocalDateTime.now());
        dirMapper.insert(md);

        // 扫描已有内容文件
        scanAndAddFiles(dir, md.getId());
        return md;
    }

    private void scanAndAddFiles(File dir, Integer dirId) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isFile() && f.length() > 0) {
                DossierFile existing = fileMapper.selectByPath(f.getAbsolutePath());
                if (existing == null) {
                    DossierFile df = new DossierFile();
                    df.setDirId(dirId);
                    df.setFilePath(f.getAbsolutePath());
                    df.setFileName(f.getName());
                    df.setFileSize(f.length());
                    df.setSourceType("WATCHED");
                    df.setStatus("ACTIVE");
                    df.setCreatedAt(LocalDateTime.now());
                    fileMapper.insert(df);
                }
            }
        }
    }

    @Override
    public List<MonitoredDirectory> getAllDirectories() {
        return dirMapper.selectByStatus("ACTIVE");
    }

    @Override
    @Transactional
    public void stopMonitoring(Integer dirId, String action) {
        if ("ARCHIVE".equals(action)) {
            dirMapper.updateStatus(dirId, "ARCHIVED");
            List<DossierFile> files = fileMapper.selectByDirId(dirId);
            for (DossierFile f : files) {
                fileMapper.updateStatus(f.getId(), "ARCHIVED");
            }
        } else if ("DELETE".equals(action)) {
            dirMapper.deleteById(dirId);
        }
    }

    // ========== 文件管理 ==========

    @Override
    @Transactional
    public DossierFile addManualFile(String filePath) {
        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) throw new IllegalArgumentException("文件不存在");
        if (f.length() == 0) throw new IllegalArgumentException("空文件不允许添加");

        DossierFile existing = fileMapper.selectByPath(filePath);
        if (existing != null) throw new IllegalArgumentException("文件已被管理");

        DossierFile df = new DossierFile();
        df.setFilePath(filePath);
        df.setFileName(f.getName());
        df.setFileSize(f.length());
        df.setSourceType("MANUAL");
        df.setStatus("ACTIVE");
        df.setCreatedAt(LocalDateTime.now());
        fileMapper.insert(df);
        return df;
    }

    @Override
    public List<DossierFile> getFilesByDirectory(Integer dirId) {
        return fileMapper.selectByDirId(dirId);
    }

    @Override
    public List<DossierFile> getManualFiles() {
        return fileMapper.selectBySourceType("MANUAL");
    }

    @Override
    public void archiveFile(Integer fileId) {
        fileMapper.updateStatus(fileId, "ARCHIVED");
    }

    @Override
    @Transactional
    public void deleteFile(Integer fileId) {
        fileMapper.deleteById(fileId);
    }

    // ========== 修订记录（45分钟合并 + 跨天强制新记录） ==========

    @Override
    @Transactional
    public FileRevision recordRevision(Integer fileId, Long fileSize) {
        LocalDateTime now = LocalDateTime.now();
        FileRevision latest = revisionMapper.selectLatestByFileId(fileId);

        if (latest != null) {
            long minutes = Duration.between(latest.getRecordedAt(), now).toMinutes();
            // 判断是否跨天（latest.getRecordedAt() 是当前修订记录的第一次保存时间）
            boolean sameDay = latest.getRecordedAt().toLocalDate().equals(now.toLocalDate());

            /*
            //以下逻辑是前一次修改时间和后一次修改时间比较（逻辑错误）：
            if (!sameDay) {
                // 跨天 → 强制新记录
            } else if (minutes <= MERGE_WINDOW_MINUTES) {
                // 同一天 + 45分钟内 → 覆盖时间
                latest.setRecordedAt(now);
                latest.setFileSize(fileSize);
                revisionMapper.update(latest);
                return latest;
            }*/

            //正确逻辑：以当前修订记录的第一次保存时间为锚点；不超过45分钟，时间不变。
            if (sameDay && minutes <= MERGE_WINDOW_MINUTES) {
                System.out.println("enter？");
                // 同一天 + 45分钟内 → 合并
                // 注意：这里应该更新的是文件大小，而不是时间
                // 时间保持不变，始终是第一次保存的时间
                latest.setFileSize(fileSize);
                revisionMapper.update(latest);
                return latest;
            }
        }

        // 新记录
        FileRevision rev = new FileRevision();
        rev.setFileId(fileId);
        rev.setVersionNumber(latest == null ? 1 : latest.getVersionNumber() + 1);
        rev.setRecordedAt(now);   // 记录第一次保存的时间
        rev.setFileSize(fileSize);
        revisionMapper.insert(rev);
        return rev;
    }

    @Override
    public List<FileRevision> getRevisions(Integer fileId) {
        return revisionMapper.selectByFileId(fileId);
    }

    // ========== 备注管理 ==========

    @Override
    public FileNote addNote(Integer fileId, Integer revisionId, String content) {
        FileNote note = new FileNote();
        note.setFileId(fileId);
        note.setRevisionId(revisionId);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    @Override
    public List<FileNote> getNotesByFile(Integer fileId) {
        return noteMapper.selectByFileId(fileId);
    }

    @Override
    public List<FileNote> getNotesByRevision(Integer revisionId) {
        return noteMapper.selectByRevisionId(revisionId);
    }

    @Override
    public List<FileNote> getFileLevelNotes(Integer fileId) {
        return noteMapper.selectFileLevelNotes(fileId);
    }

    @Override
    public DossierFile getFileById(Integer id) {
        return fileMapper.selectById(id);
    }
}
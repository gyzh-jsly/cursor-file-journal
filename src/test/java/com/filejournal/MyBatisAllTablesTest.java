package com.filejournal;

import com.filejournal.mapper.CommitLogMapper;
import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.mapper.WatchedFolderMapper;
import com.filejournal.model.CommitLog;
import com.filejournal.model.WatchedFile;
import com.filejournal.model.WatchedFolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class MyBatisAllTablesTest {

    @Autowired
    private WatchedFolderMapper watchedFolderMapper;
    @Autowired
    private WatchedFileMapper watchedFileMapper;
    @Autowired
    private CommitLogMapper commitLogMapper;

    @Test
    void watchedFolderInsertAndSelectAll() {
        WatchedFolder folder = newFolder();
        assertEquals(1, watchedFolderMapper.insert(folder));
        assertNotNull(folder.getId());

        List<WatchedFolder> all = watchedFolderMapper.selectAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(f -> folder.getId().equals(f.getId())));
    }

    @Test
    void watchedFileInsertAndSelectAll() {
        WatchedFolder folder = newFolder();
        watchedFolderMapper.insert(folder);

        WatchedFile file = new WatchedFile();
        file.setFolderId(folder.getId());
        file.setFilePath("/test/file-" + UUID.randomUUID() + ".txt");
        file.setFileName("test file");
        file.setFirstSeenTime(LocalDateTime.now());
        file.setFirstWeather("sunny");
        file.setCommitCount(0);
        file.setLastCommitTime(LocalDateTime.now());
        file.setLastMessage("test commit");
        file.setCreatedAt(LocalDateTime.now());
        assertEquals(1, watchedFileMapper.insert(file));
        assertNotNull(file.getId());

        List<WatchedFile> all = watchedFileMapper.selectAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(f -> file.getId().equals(f.getId())));
    }

    @Test
    void commitLogInsertAndSelectAll() {
        WatchedFolder folder = newFolder();
        watchedFolderMapper.insert(folder);

        WatchedFile file = new WatchedFile();
        file.setFolderId(folder.getId());
        file.setFilePath("/test/cl-" + UUID.randomUUID() + ".txt");
        file.setFileName("test file");
        file.setFirstSeenTime(LocalDateTime.now());
        file.setFirstWeather("sunny");
        file.setCommitCount(0);
        file.setLastCommitTime(LocalDateTime.now());
        file.setLastMessage("test commit");
        file.setCreatedAt(LocalDateTime.now());
        watchedFileMapper.insert(file);

        CommitLog log = new CommitLog();
        log.setFileId(file.getId());
        log.setMessage("test commit");
        log.setCommitTime(LocalDateTime.now());
        assertEquals(1, commitLogMapper.insert(log));
        assertNotNull(log.getId());

        List<CommitLog> all = commitLogMapper.selectAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(c -> log.getId().equals(c.getId())));
    }

    private static WatchedFolder newFolder() {
        WatchedFolder folder = new WatchedFolder();
        folder.setFolderPath("/tmp/fj-test-" + UUID.randomUUID());
        folder.setCreatedAt(LocalDateTime.now());
        folder.setFolderName("test folder");
        return folder;
    }
}

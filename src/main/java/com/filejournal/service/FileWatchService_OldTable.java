/*
package com.filejournal.service;

import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.mapper.WatchedFolderMapper;
import com.filejournal.model.WatchedFile;
import com.filejournal.model.WatchedFolder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class FileWatchService_OldTable implements DisposableBean {

    @Autowired
    private WatchedFolderMapper folderMapper;
    @Autowired
    private WatchedFileMapper fileMapper;

    private WatchService watchService;
    private final Map<WatchKey, Path> keyToPath = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    private Thread watchThread;

    // 并发控制锁（第一版方案核心）
    private final ConcurrentHashMap<String, Boolean> processingFiles = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        try {
            //拿到监听器
            watchService = FileSystems.getDefault().newWatchService();
            List<WatchedFolder> folders = folderMapper.selectAll();
            for (WatchedFolder folder : folders) {
                registerFolder(Paths.get(folder.getFolderPath()));
            }
            watchThread = new Thread(this::processEvents);
            watchThread.setDaemon(true);
            watchThread.start();
            System.out.println("文件监视服务已启动，监视 " + folders.size() + " 个文件夹");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerFolder(Path folderPath) throws Exception {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) return;
        //注册要监听的目录
        WatchKey key = folderPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        keyToPath.put(key, folderPath);
    }

    private void processEvents() {
        while (running) {
            try {
                //轮询取事件
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;
                Path dir = keyToPath.get(key);
                if (dir == null) { key.reset(); continue; }

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path relativePath = (Path) event.context();
                    Path absolutePath = dir.resolve(relativePath);
                    //处理事件
                    handleFileEvent(absolutePath);
                }
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileEvent(Path filePath) {
        File file = filePath.toFile();
        String absolutePath = file.getAbsolutePath();
        String fileName = file.getName();
    
        // 过滤隐藏文件
        try {
            if (Files.isHidden(filePath)) return;
        } catch (IOException e) { return; }
    
        // 物理文件存在性校验
        if (!file.exists() || !file.isFile()) return;
    
        // 直接尝试插入，数据库会处理重复
        try {
            Optional<Long> folderIdOpt = findFolderId(filePath);
            if (!folderIdOpt.isPresent()) return;
    
            WatchedFile newFile = new WatchedFile();
            newFile.setFolderId(folderIdOpt.get());
            newFile.setFilePath(absolutePath);
            newFile.setFileName(fileName);
            newFile.setCreatedAt(LocalDateTime.now());
            
            int rows = fileMapper.insert(newFile);
            if (rows > 0) {
                System.out.println("✅ 发现新文件，已纳入管理: " + fileName);
            }
        } catch (DuplicateKeyException e) {
            // 重复插入，静默忽略
        } catch (Exception e) {
            System.err.println("❌ 处理文件事件失败: " + absolutePath);
            e.printStackTrace();
        }
    }

    private Optional<Long> findFolderId(Path filePath) {
        Path parent = filePath.getParent();
        List<WatchedFolder> folders = folderMapper.selectAll();
        for (WatchedFolder folder : folders) {
            if (Paths.get(folder.getFolderPath()).equals(parent)) {
                return Optional.of(folder.getId());
            }
        }
        return Optional.empty();
    }

    @Override
    public void destroy() throws Exception {
        running = false;
        if (watchThread != null) watchThread.interrupt();
        if (watchService != null) watchService.close();
    }
}*/

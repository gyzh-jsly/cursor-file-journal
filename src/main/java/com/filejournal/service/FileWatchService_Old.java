/*
package com.filejournal.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.mapper.WatchedFolderMapper;
import com.filejournal.model.WatchedFile;
import com.filejournal.model.WatchedFolder;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
//5.2创建文件监视服务
@Service
public class FileWatchService_Old implements DisposableBean{

    @Autowired
    private WatchedFolderMapper folderMapper;

    @Autowired
    private WatchedFileMapper fileMapper;
    private volatile boolean running = true;  // 控制线程运行的标志
    private Thread watchThread;

    private final Map<WatchKey, Path> keyToPath = new ConcurrentHashMap<>();
    private WatchService watchService;

    // 防止并发重复处理同一个文件
    private final ConcurrentHashMap<String, Boolean> processingFiles = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            
            // 获取所有被监视的文件夹
            List<WatchedFolder> folders = folderMapper.selectAll();
            for (WatchedFolder folder : folders) {
                registerFolder(Paths.get(folder.getFolderPath()));
            }
            
            // 启动监听线程
            watchThread = new Thread(this::processEvents);
            watchThread.setDaemon(true);
            watchThread.start();
            
            System.out.println("文件监视服务已启动，监视 " + folders.size() + " 个文件夹");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerFolder(Path folderPath) throws Exception {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return;
        }
        WatchKey key = folderPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        keyToPath.put(key, folderPath);
        System.out.println("已注册监视文件夹: " + folderPath);
    }

    private void processEvents() {
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);  // 用 poll 替代 take，可响应中断
                if (key == null) {
                    continue;
                }
                Path dir = keyToPath.get(key);
                if (dir == null) {
                    key.reset();
                    continue;
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedFile = dir.resolve((Path) event.context());
                    handleFileEvent(changedFile);
                }
                
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件监视服务已停止");
    }

    private void handleFileEvent(Path filePath) {
        File file = filePath.toFile();
        String absolutePath = file.getAbsolutePath();

        */
/**出现报错：
         * 1.业务现象：新建一个 ttttt.txt，却出现了两个文件（ttttt.txt 和 新建文本文档.txt）
         * 2.根本错误：HikariDataSource has been closed，数据库连接池被关闭了
         * 解决方案：增加一个 文件存在性校验 + 去重逻辑：
         * 
        *//*

        //===关键修复1：检查物理文件是否真实存在（过滤掉临时文件和已删除文件）
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不是普通文件，跳过: " + absolutePath);
            return;
        }
        // === 关键修复 2：并发控制，防止同一文件被多个线程同时处理 ===
        if (processingFiles.putIfAbsent(absolutePath, Boolean.TRUE) != null) {
            System.out.println("文件正在处理中，跳过: " + file.getName());
            return;
        }

        try {
            // === 关键修复 3：再次检查数据库，确认是否已存在 ===
            WatchedFile existing = fileMapper.selectByFilePath(absolutePath);
            if (existing != null) {
                System.out.println("文件已在数据库中，跳过: " + file.getName());
                return;
            }

            // === 关键修复 4：查找所属文件夹 ===
            Optional<Long> folderIdOpt = findFolderId(filePath);
            if (!folderIdOpt.isPresent()) {
                System.out.println("未找到所属文件夹，跳过: " + absolutePath);
                return;
            }

            // === 关键修复 5：插入数据库 ===
            // 新文件：自动纳入管理
            WatchedFile newFile = new WatchedFile();
            newFile.setFolderId(folderIdOpt.get());
            newFile.setFilePath(absolutePath);
            newFile.setFileName(file.getName());  // 使用当前真实的文件名
            newFile.setCreatedAt(LocalDateTime.now());

            fileMapper.insert(newFile);
            System.out.println("✅ 发现新文件，已纳入管理: " + file.getName());
        }  catch (DuplicateKeyException e) {
            // 极少数情况下的并发冲突，静默处理
            System.out.println("⚠️ 文件已存在（并发冲突），忽略: " + file.getName());
        } catch (Exception e) {
            System.err.println("❌ 处理文件事件失败: " + absolutePath);
            e.printStackTrace();
        } finally {
            // === 关键修复 6：清理处理标记 ===
            processingFiles.remove(absolutePath);
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
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            watchService.close();
        }
        System.out.println("FileWatchService 已销毁");
    }
}
*/

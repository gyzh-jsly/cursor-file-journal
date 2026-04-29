package com.filejournal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filejournal.mapper.CommitLogMapper;
import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.model.CommitLog;
import com.filejournal.model.WatchedFile;
import com.filejournal.service.CommitService;
import com.filejournal.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommitServiceImpl implements CommitService {

    @Autowired
    private WatchedFileMapper fileMapper;

    @Autowired
    private CommitLogMapper commitLogMapper;

    @Autowired
    private WeatherService weatherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void commit(Long fileId, String message) {
        // 1. 查询文件信息
        WatchedFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在，ID: " + fileId);
        }

        // 2. 检查物理文件是否存在
        Path filePath = Paths.get(file.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("物理文件不存在: " + file.getFilePath());
        }

        // 3. 计算文件 MD5（判断是否真正修改，可选）
        String currentMd5 = computeMd5(filePath);

        // 4. 判断是否为首次 Commit（决定是否获取天气）
        boolean isFirstCommit = (file.getCommitCount() == null || file.getCommitCount() == 0);
        String weather = null;
        if (isFirstCommit) {
            weather = weatherService.getCurrentWeather();
            file.setFirstSeenTime(LocalDateTime.now());
            file.setFirstWeather(weather);
        }

        // 5. 更新文件统计信息
        file.setCommitCount(file.getCommitCount() == null ? 1 : file.getCommitCount() + 1);
        file.setLastCommitTime(LocalDateTime.now());
        file.setLastMessage(message);
        fileMapper.update(file);

        // 6. 保存 CommitLog 到数据库
        CommitLog log = new CommitLog();
        log.setFileId(fileId);
        log.setCommitTime(LocalDateTime.now());
        log.setMessage(message);
        commitLogMapper.insert(log);

        // 7. 追加记录到隐藏 Log 文件
        appendToLogFile(filePath, log, weather);
    }

    /**
     * 计算文件 MD5
     */
    private String computeMd5(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算 MD5 失败: " + filePath, e);
        }
    }

    /**
     * 追加记录到隐藏 Log 文件（JSON Lines 格式）
     */
    private void appendToLogFile(Path filePath, CommitLog log, String weather) {
        Path parent = filePath.getParent();
        String logFileName = "." + filePath.getFileName().toString() + ".log";
        Path logFilePath = parent.resolve(logFileName);

        try {
            Map<String, Object> record = new HashMap<>();
            record.put("id", log.getId());
            record.put("timestamp", System.currentTimeMillis());
            record.put("weather", weather);
            record.put("message", log.getMessage());

            String jsonLine = objectMapper.writeValueAsString(record) + "\n";
            Files.write(logFilePath, jsonLine.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Log 文件写入失败不影响主流程，只打印日志
            System.err.println("写入 Log 文件失败: " + logFilePath + ", 错误: " + e.getMessage());
        }
    }

    @Override
    public List<CommitLog> getCommitHistory(Long fileId) {
        return commitLogMapper.selectByFileId(fileId);
    }
}

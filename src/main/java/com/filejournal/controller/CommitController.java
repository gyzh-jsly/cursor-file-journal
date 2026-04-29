package com.filejournal.controller;

import com.filejournal.mapper.WatchedFileMapper;
import com.filejournal.model.WatchedFile;
import com.filejournal.service.CommitService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
//4
@Controller
@RequestMapping("/commit")
public class CommitController {

    @Autowired
    private CommitService commitService;
    @Autowired
    private WatchedFileMapper fileMapper;

    @PostMapping("/add")
    @ResponseBody
    public String addCommit(@RequestParam Long fileId,
                            @RequestParam(required = false) String message) {
        try {
            commitService.commit(fileId, message != null ? message : "");
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    //5
    @GetMapping("/history/{fileId}")
    @ResponseBody
    public Map<String, Object> getHistory(@PathVariable Long fileId) {
        Map<String, Object> result = new HashMap<>();
        // 获取文件基本信息
        WatchedFile file = fileMapper.selectById(fileId);
        result.put("file", file);
        // 获取 Commit 历史
        result.put("commits", commitService.getCommitHistory(fileId));
        return result;
    }
}
package com.filejournal.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.filejournal.model.WatchedFolder;
import com.filejournal.service.FolderService;

@Controller
public class HomeController {
/** 原返回首页，下为现返回首页
    @GetMapping("/")
    public String index() {
        return "index";
    }
*/

    @Autowired
    private FolderService folderService;

    // @GetMapping("/")
    // public String index(Model model) {
    //     List<WatchedFolder> folders = folderService.getAllFolders();
    //     model.addAttribute("folders", folders);
    //     return "index2";
    // }

    @GetMapping("/")
    public String index(Model model) {
        // 获取所有文件夹（用于左侧列表）
        List<WatchedFolder> folders = folderService.getAllFolders();
        model.addAttribute("folders", folders);

        // 如果有文件夹，默认选中第一个，加载其聚合流数据
        if (!folders.isEmpty()) {
            WatchedFolder firstFolder = folders.get(0);
            model.addAttribute("currentFolder", firstFolder);
            model.addAttribute("files", folderService.getFolderAggregate(firstFolder.getId()));
        } else {
            model.addAttribute("files", Collections.emptyList());
        }

        // 首页卡片统计数据（可选）
        model.addAttribute("folderCount", folders.size());
        model.addAttribute("diaryCount", 0);  // 待实现
        model.addAttribute("todayDiaryCount", 0);
        model.addAttribute("oldestDiaryDate", null);

        return "index3";
    }
}

package com.filejournal.controller;

import com.filejournal.mapper.WatchedFolderMapper;
import com.filejournal.model.WatchedFileWithLastCommit;
import com.filejournal.model.WatchedFolder;
import com.filejournal.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private WatchedFolderMapper folderMapper;

    /**
     * 添加文件夹（POST 请求，表单提交）
     */
    @PostMapping("/add")
    public String addFolder(@RequestParam String folderPath,
                            @RequestParam(required = false) String folderName,
                            RedirectAttributes redirectAttributes) {
        try {
            folderService.addFolder(folderPath, folderName);
            redirectAttributes.addFlashAttribute("success", "文件夹添加成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    /**
     * 获取所有文件夹（JSON 格式，供 Ajax 调用）
     */
    @GetMapping("/all")
    @ResponseBody
    public List<WatchedFolder> getAllFolders() {
        return folderService.getAllFolders();
    }

    /**
     * 删除文件夹
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public String deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return "success";
    }


    /**
     * 查看指定文件夹的聚合流页面
     */
    @GetMapping("/view/{folderId}")
    public String viewFolder(@PathVariable Long folderId, Model model) {
        // 获取当前文件夹信息
        WatchedFolder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            return "redirect:/";
        }
        
        // 获取所有文件夹列表（用于左侧导航）
        List<WatchedFolder> allFolders = folderService.getAllFolders();

        Map<Long, Integer> folderFileCounts = new HashMap<>();
        for (WatchedFolder f : allFolders) {
            folderFileCounts.put(f.getId(), folderService.countFilesInFolder(f.getId()));
        }

        // 获取当前文件夹的聚合流数据
        List<WatchedFileWithLastCommit> files = folderService.getFolderAggregate(folderId);

        model.addAttribute("folders", allFolders);
        model.addAttribute("folderFileCounts", folderFileCounts);
        model.addAttribute("currentFolder", folder);
        model.addAttribute("files", files);
        
        return "index3";  // 新页面，用于展示聚合流
    }

    @GetMapping("/view")
    public String folderView(Model model) {
        List<WatchedFolder> folders = folderService.getAllFolders();
        model.addAttribute("folders", folders);
        if (!folders.isEmpty()) {
            model.addAttribute("currentFolder", folders.get(0));
            model.addAttribute("files", folderService.getFolderAggregate(folders.get(0).getId()));
        }
        return "folder/detail2";  // 返回 index，但前端会根据 URL 切换到 diaryView
    }

    @GetMapping("/aggregate/{folderId}")
    public String getAggregate(@PathVariable Long folderId, Model model) {
        model.addAttribute("files", folderService.getFolderAggregate(folderId));
        model.addAttribute("currentFolder", folderMapper.selectById(folderId));
        return "folder/detail2 :: .stream-list";
    }
}
package com.filejournal.controller;

import com.filejournal.model.*;
import com.filejournal.service.DossierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dossier")
public class DossierController {

    @Autowired
    private DossierService dossierService;

    // 页面入口
    @GetMapping
    public String index(Model model) {
        model.addAttribute("directories", dossierService.getAllDirectories());
        model.addAttribute("manualFiles", dossierService.getManualFiles());
        return "dossier/index";
    }

    // 目录管理
    @PostMapping("/directory/add")
    @ResponseBody
    public MonitoredDirectory addDirectory(@RequestParam String dirPath,
                                           @RequestParam(required = false) String dirName) {
        return dossierService.addDirectory(dirPath, dirName);
    }

    @PostMapping("/directory/stop/{id}")
    @ResponseBody
    public String stopMonitoring(@PathVariable Integer id, @RequestParam String action) {
        dossierService.stopMonitoring(id, action);
        return "success";
    }

    // 文件管理
    @PostMapping("/file/add")
    @ResponseBody
    public DossierFile addManualFile(@RequestParam String filePath) {
        return dossierService.addManualFile(filePath);
    }

    @GetMapping("/file/{id}/revisions")
    @ResponseBody
    public List<FileRevision> getRevisions(@PathVariable Integer id) {
        return dossierService.getRevisions(id);
    }

    @PostMapping("/file/{id}/archive")
    @ResponseBody
    public String archiveFile(@PathVariable Integer id) {
        dossierService.archiveFile(id);
        return "success";
    }

    @DeleteMapping("/file/{id}")
    @ResponseBody
    public String deleteFile(@PathVariable Integer id) {
        dossierService.deleteFile(id);
        return "success";
    }

    // 备注管理
    @PostMapping("/note/add")
    @ResponseBody
    public FileNote addNote(@RequestParam Integer fileId,
                            @RequestParam(required = false) Integer revisionId,
                            @RequestParam String content) {
        return dossierService.addNote(fileId, revisionId, content);
    }

    @GetMapping("/file/{id}/notes")
    @ResponseBody
    public List<FileNote> getNotes(@PathVariable Integer id) {
        return dossierService.getNotesByFile(id);
    }

    //返回监控目录片段
    @GetMapping("/monitor-pane")
    public String monitorPane() {
        return "dossier/monitor";
    }

    // 获取所有活跃目录列表（JSON格式，供Ajax使用）
    @GetMapping("/directory/list")
    @ResponseBody
    public List<MonitoredDirectory> listDirectories() {
        return dossierService.getAllDirectories();
    }

    // 获取指定目录下的文件列表（JSON格式）
    @GetMapping("/file/list")
    @ResponseBody
    public List<DossierFile> listFiles(@RequestParam Integer dirId) {
        return dossierService.getFilesByDirectory(dirId);
    }

    // 返回文件详情页的 HTML 片段
    @GetMapping("/file/{id}/detail")
    public String fileDetail(@PathVariable Integer id, Model model) {
        //原有代码
        DossierFile file = dossierService.getFileById(id);
        List<FileRevision> revisions = dossierService.getRevisions(id);
        List<FileNote> notes = dossierService.getNotesByFile(id);

        model.addAttribute("file", file);
        model.addAttribute("revisions", revisions);
        model.addAttribute("notes", notes);

        // 直接把 id 传给模板，不需要依赖 file 对象
        model.addAttribute("fileId", id);
        return "dossier/file-detail";
    }

    // 返回文件详情页的 JSON 数据（供 Ajax 局部刷新用）
    @GetMapping("/file/{id}/detail-data")
    @ResponseBody
    public Map<String, Object> fileDetailData(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        result.put("file", dossierService.getFileById(id));
        result.put("revisions", dossierService.getRevisions(id));
        result.put("notes", dossierService.getNotesByFile(id));
        return result;
    }
}
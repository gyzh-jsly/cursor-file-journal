package com.filejournal.controller;

import com.filejournal.model.Diary;
import com.filejournal.model.DiaryComment;
import com.filejournal.model.DiaryNote;
import com.filejournal.model.ImportBatch;
import com.filejournal.service.DiaryCommentService;
import com.filejournal.service.DiaryNoteService;
import com.filejournal.service.DiaryService;
import com.filejournal.service.ImportBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;
    @Autowired
    private DiaryNoteService noteService;
    @Autowired
    private DiaryCommentService commentService;
    @Autowired
    private ImportBatchService batchService;

    // 年历页面（固定标签页内容）
    @GetMapping("/calendar")
    public String calendar(Model model) {
        return "diary/calendar";
    }

    // 获取月历数据
    @GetMapping("/calendar/data")
    @ResponseBody
    public Map<String, Integer> getCalendarData(@RequestParam Integer year,
                                                 @RequestParam Integer month) {
        List<Map<String, Object>> list = diaryService.getCalendarData(year, month);
        Map<String,Integer> result = new HashMap<>();
        for (Map<String,Object> m : list) {
            String date = m.get("diary_date").toString();
            Integer count = ((Number)m.get("count")).intValue();
            result.put(date,count);
        }
        return result;
    }

    //年历跳月历（这个怎么和上面的获取月历数据很像啊，他们是不是可以结合起来？）
    @GetMapping("/calendar/month")
    public String calendarPage(@RequestParam Integer year, @RequestParam Integer month, Model model) {
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        return "diary/month-calendar";
    }

    @GetMapping("/calendar/content")
    public String calendarContent(Model model) {
        // 只返回挂历的内容片段，不包含完整 HTML 骨架
        return "diary/calendar";
    }

    @GetMapping("/calendar/year-stats")
    @ResponseBody
    public Map<String, Integer> getYearStats(@RequestParam Integer year) {
        return diaryService.getYearStats(year);
    }

    @GetMapping("/write-page")
    public String writePage() {
        return "diary/write-diary";
    }

    // 某天的日记列表（标签页内容）
    @GetMapping("/list")
    public String list(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                       @RequestParam(defaultValue = "created") String sort,
                       Model model) {
        model.addAttribute("date", date);
        model.addAttribute("diaries", diaryService.getDiariesByDate(date, sort));
        model.addAttribute("sort", sort);
        return "diary/list";
    }

    // 日记全文页
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Diary diary = diaryService.getDiary(id);
        model.addAttribute("diary", diary);
        model.addAttribute("canEdit", diaryService.canEdit(diary));
        model.addAttribute("createdAtStr", diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        if (diary.getLastModifiedAt() != null) {
            model.addAttribute("modifiedAtStr", diary.getLastModifiedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return "diary/detail";
    }

    // 创建日记
    @PostMapping("/create")
    @ResponseBody
    public Diary create(@RequestParam String content,
                        @RequestParam(required = false) Boolean isPinned) {
        return diaryService.createDiary(content, isPinned);
    }

    // 更新日记
    @PostMapping("/update/{id}")
    @ResponseBody
    public Diary update(@PathVariable Integer id,
                        @RequestParam String content,
                        @RequestParam(required = false) Boolean isPinned) {
        return diaryService.updateDiary(id, content, isPinned);
    }


    // 获取全文备注列表
    @GetMapping("/{id}/notes")
    @ResponseBody
    public List<DiaryNote> getNotes(@PathVariable Integer id) {
        return noteService.getNotesByDiaryId(id);
    }

    // 新增全文备注
    @PostMapping("/{id}/notes")
    @ResponseBody
    public DiaryNote addNote(@PathVariable Integer id, @RequestParam String content) {
        return noteService.addNote(id, content);
    }

    // 获取段评列表
    @GetMapping("/{id}/comments")
    @ResponseBody
    public List<DiaryComment> getComments(@PathVariable Integer id) {
        return commentService.getCommentsByDiaryId(id);
    }

    // 新增段评
    @PostMapping("/{id}/comments")
    @ResponseBody
    public DiaryComment addComment(@PathVariable Integer id,
                                @RequestParam String selectedText,
                                @RequestParam Integer startOffset,
                                @RequestParam String content) {
        return commentService.addComment(id, selectedText, startOffset, content);
    }

    //删除段评
    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseBody
    public String deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return "success";
    }


    // 获取所有导入批次
    @GetMapping("/import-batches")
    @ResponseBody
    public List<ImportBatch> listBatches() {
        return batchService.getAllBatches();
    }

    // 删除导入批次
    @DeleteMapping("/import-batch/{id}")
    @ResponseBody
    public String deleteBatch(@PathVariable Integer id) {
        batchService.deleteBatch(id);
        return "success";
    }

}
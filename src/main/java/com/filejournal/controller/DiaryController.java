package com.filejournal.controller;

import com.filejournal.model.Diary;
import com.filejournal.model.DiaryComment;
import com.filejournal.model.DiaryNote;
import com.filejournal.service.DiaryCommentService;
import com.filejournal.service.DiaryNoteService;
import com.filejournal.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // 月历页面（固定标签页内容）
    @GetMapping("/calendar")
    public String calendar(Model model) {
        return "diary/calendar";
    }

    // 获取月历数据
    @GetMapping("/calendar/data")
    @ResponseBody
    public Map<String, Integer> getCalendarData(@RequestParam Integer year,
                                                 @RequestParam Integer month) {
        return diaryService.getCalendarData(year, month);
    }

    @GetMapping("/calendar/content")
    public String calendarContent(Model model) {
        // 只返回挂历的内容片段，不包含完整 HTML 骨架
        return "diary/calendar :: .calendar-container";
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
}
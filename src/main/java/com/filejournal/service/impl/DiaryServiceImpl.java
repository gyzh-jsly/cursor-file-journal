package com.filejournal.service.impl;

import com.filejournal.mapper.DiaryMapper;
import com.filejournal.model.Diary;
import com.filejournal.service.DiaryService;
import com.filejournal.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DiaryServiceImpl implements DiaryService {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    // private static final String CALENDAR_CACHE_KEY = "calendar:%d:%d";

    @Override
    @Transactional
    public Diary createDiary(String content, Boolean isPinned) {
        LocalDateTime now = LocalDateTime.now();
        Diary diary = new Diary();
        diary.setContent(content);
        diary.setCreatedAt(now);
        diary.setEditDeadline(calculateDeadline(now));
        diary.setModifyCount(0);
        diary.setFirstWeather(weatherService.getCurrentWeather());
        diary.setIsPinned(isPinned != null && isPinned ? 1 : 0);
        diary.setDiaryDate(now.toLocalDate());
        diary.setSourceType("WRITE");
        diary.setIsFrozen(0);
        diaryMapper.insert(diary);
          
        // 1. 存入日记详情缓存（可选，如果后续有详情页再用）
        String detailKey = "diary:detail:" + diary.getId();
        redisTemplate.opsForValue().set(detailKey, diary,1,TimeUnit.DAYS);
        // 2. 清除该年份的缓存
        String yearStatsKey = "diary:year-stats:" + diary.getDiaryDate().getYear();
        redisTemplate.delete(yearStatsKey);
        
        return diary;
    }

    @Override
    @Transactional
    public Diary updateDiary(Integer id, String content, Boolean isPinned) {
        Diary diary = diaryMapper.selectById(id);
        if (diary == null) {
            throw new IllegalArgumentException("日记不存在");
        }
        if (!canEdit(diary)) {
            throw new IllegalStateException("日记已锁定或修改次数已达上限");
        }
        diary.setContent(content);
        diary.setLastModifiedAt(LocalDateTime.now());
        diary.setModifyCount(diary.getModifyCount() + 1);
        diary.setIsPinned(isPinned != null && isPinned ? 1 : 0);
        diaryMapper.update(diary);
        return diary;
    }

    @Override
    public Diary getDiary(Integer id) {
        return diaryMapper.selectById(id);
    }

    @Override
    public List<Diary> getDiariesByDate(LocalDate date, String sort) {
        return diaryMapper.selectByDate(date, sort);
    }

    @Override
    public List<Map<String, Object>> getCalendarData(Integer year, Integer month) {
        return diaryMapper.selectCalendarData(year, month);
    }

    @Override
    public boolean canEdit(Diary diary) {
        if (LocalDateTime.now().isAfter(diary.getEditDeadline())) {
            return false;
        }
        return diary.getModifyCount() < 3;
    }

    @Override
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        int hour = createdAt.getHour();
        if (hour < 12) {
            return createdAt.withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else {
            return createdAt.plusHours(24);
        }
    }

	@Override
	// public Map<Integer, Integer> getYearStats(Integer year) {
    //     Map<Integer, Integer> result = new HashMap<>();
    //     // 初始化1-12月为0
    //     for (int m = 1; m <= 12; m++) {
    //         result.put(m, 0);
    //     }
        
    //     // 查询该年各月的日记篇数
    //     List<Map<String, Object>> stats = diaryMapper.selectYearStats(year);
    //     for (Map<String, Object> stat : stats) {
    //         Integer month = (Integer) stat.get("month");
    //         Long count = (Long) stat.get("count");
    //         result.put(month, count.intValue());
    //     }
    //     return result;
    // }
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getYearStats(Integer year) {
        String cacheKey = "diary:year-stats:" + year;
        
        // 1. 查 Redis
        // Map<String, Integer> cached = (Map<String, Integer>) redisTemplate.opsForValue().get(cacheKey);
        // if (cached != null) {
        //     return cached;
        // }
        // 从 Redis 读取
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        if (cachedObj instanceof Map) {
            return (Map<String, Integer>) cachedObj;
        }
        
        // 2. 查数据库
        Map<String, Integer> result = new HashMap<>();
        for (int m = 1; m <= 12; m++) result.put(String.valueOf(m), 0);
        List<Map<String, Object>> stats = diaryMapper.selectYearStats(year);
        for (Map<String, Object> stat : stats) {
            Integer month = (Integer) stat.get("month");
            Long count = (Long) stat.get("count");
            result.put(String.valueOf(month), count.intValue());
        }
        
        // 3. 存入 Redis（过期时间 1 小时，足够覆盖用户反复切换）
        redisTemplate.opsForValue().set(cacheKey, result, 1, TimeUnit.HOURS);
        
        return result;
    }
}
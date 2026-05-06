package com.filejournal.mapper;


import com.filejournal.model.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DiaryMapper {
    int insert(Diary diary);
    int update(Diary diary);
    Diary selectById(Integer id);
    List<Diary> selectByDate(@Param("date") LocalDate date, @Param("sort") String sort);
    List<Map<String, Object>> selectCalendarData(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 查询指定年份各月的日记篇数
     */
    List<Map<String, Object>> selectYearStats(@Param("year") Integer year);

    List<Map<String, Object>> selectImportBatches();
    List<Diary> selectByBatchId(Integer batchId);
    int updateFrozenStatus(Integer id);
}
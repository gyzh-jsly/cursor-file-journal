package com.filejournal.mapper;

import com.filejournal.model.ImportBatch;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImportBatchMapper {
    int insert(ImportBatch batch);
    ImportBatch selectById(Integer id);
    List<ImportBatch> selectAll();
    int updateCount(Integer id, Integer count);
    int deleteById(Integer id);
}

package com.filejournal.mapper;

import com.filejournal.model.DossierFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DossierFileMapper {
    int insert(DossierFile file);
    DossierFile selectById(Integer id);
    DossierFile selectByPath(String filePath);
    List<DossierFile> selectByDirId(Integer dirId);
    List<DossierFile> selectBySourceType(String sourceType);
    List<DossierFile> selectByStatus(String status);
    int update(DossierFile file);
    int updateStatus(Integer id, String status);
    int deleteById(Integer id);
}
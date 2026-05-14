package com.filejournal.mapper;

import com.filejournal.model.FileRevision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileRevisionMapper {
    int insert(FileRevision revision);
    FileRevision selectById(Integer id);
    List<FileRevision> selectByFileId(@Param("fileId") Integer fileId);
    FileRevision selectLatestByFileId(Integer fileId);
    int updateRecordedAt(@Param("id") Integer id, @Param("recordedAt") String recordedAt);
    int update(FileRevision revision);
}
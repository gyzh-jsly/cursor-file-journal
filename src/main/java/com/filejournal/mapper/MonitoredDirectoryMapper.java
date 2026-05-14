package com.filejournal.mapper;

import com.filejournal.model.MonitoredDirectory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MonitoredDirectoryMapper {
    int insert(MonitoredDirectory dir);
    MonitoredDirectory selectById(Integer id);
    MonitoredDirectory selectByPath(String dirPath);
    List<MonitoredDirectory> selectAll();
    List<MonitoredDirectory> selectByStatus(String status);
    int updateStatus(Integer id, String status);
    int deleteById(Integer id);
}
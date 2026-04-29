package com.filejournal.mapper;

import com.filejournal.model.WatchedFolder;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface WatchedFolderMapper {
    int insert(WatchedFolder folder);
    List<WatchedFolder> selectAll();
    WatchedFolder selectById(Long id);
    int deleteById(Long id);
}
package com.filejournal.mapper;

import com.filejournal.model.FileNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileNoteMapper {
    int insert(FileNote note);
    List<FileNote> selectByFileId(Integer fileId);
    List<FileNote> selectByRevisionId(Integer revisionId);
    List<FileNote> selectFileLevelNotes(Integer fileId);  // revision_id IS NULL
    int update(Integer id, String content);
    int deleteById(Integer id);
}
package com.filejournal.service;

import com.filejournal.model.ImportBatch;

import java.util.List;

public interface ImportBatchService {
    ImportBatch createBatch(String fileName);
    void updateBatchCount(Integer batchId, int count);
    List<ImportBatch> getAllBatches();
    void deleteBatch(Integer batchId);
}

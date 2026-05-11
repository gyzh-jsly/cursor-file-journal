package com.filejournal.service.impl;

import com.filejournal.mapper.ImportBatchMapper;
import com.filejournal.model.ImportBatch;
import com.filejournal.service.ImportBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImportBatchServiceImpl implements ImportBatchService {

    @Autowired
    private ImportBatchMapper batchMapper;

    @Override
    public ImportBatch createBatch(String fileName) {
        ImportBatch batch = new ImportBatch();
        batch.setFileName(fileName);
        batch.setBatchName("导入批次"+LocalDateTime.now());
        batch.setImportCount(0);
        batch.setCreatedAt(LocalDateTime.now());
        batchMapper.insert(batch);
        return batch;
    }

    @Override
    public void updateBatchCount(Integer batchId, int count) {
        batchMapper.updateCount(batchId, count);
    }

    @Override
    public List<ImportBatch> getAllBatches() {
        return batchMapper.selectAll();
    }

    @Override
    public void deleteBatch(Integer batchId) {
        batchMapper.deleteById(batchId);
    }
}
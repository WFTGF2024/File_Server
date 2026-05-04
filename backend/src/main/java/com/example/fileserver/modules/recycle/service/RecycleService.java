package com.example.fileserver.modules.recycle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;

public interface RecycleService {

    IPage<FsRecycleRecord> listRecycleRecords(Long userId, int pageNum, int pageSize);

    void restoreFile(Long userId, Long recordId);

    void permanentDelete(Long userId, Long recordId);

    void cleanExpiredRecords();
}

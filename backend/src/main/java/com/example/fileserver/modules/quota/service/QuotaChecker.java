package com.example.fileserver.modules.quota.service;

import com.example.fileserver.modules.quota.entity.FsUserQuota;

public interface QuotaChecker {

    boolean checkStorageQuota(Long userId, long fileSize);

    boolean checkSingleFileSize(Long userId, long fileSize);

    boolean checkBatchUploadCount(Long userId, int count);

    boolean checkFileType(Long userId, String fileExt);

    FsUserQuota getUserQuota(Long userId);

    void updateUsedStorage(Long userId, long deltaBytes);
}

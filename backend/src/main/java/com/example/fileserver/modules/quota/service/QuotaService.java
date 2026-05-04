package com.example.fileserver.modules.quota.service;

import com.example.fileserver.modules.quota.entity.FsUserQuota;

public interface QuotaService {

    FsUserQuota getUserQuota(Long userId);

    void updateUsedStorage(Long userId, long deltaBytes);

    void updateUserQuota(Long userId, FsUserQuota quota);
}

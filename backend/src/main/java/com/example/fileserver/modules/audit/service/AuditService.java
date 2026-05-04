package com.example.fileserver.modules.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.modules.audit.entity.FsFileAuditLog;

public interface AuditService {

    void log(Long userId, String userType, String action, String resourceType,
             Long resourceId, String resourceName, String detail,
             String clientIp, String userAgent, int result, String errorMessage);

    void log(Long userId, String userType, String action, String resourceType,
             Long resourceId, String resourceName);

    IPage<FsFileAuditLog> queryAuditLogs(int pageNum, int pageSize, Long userId, String action);

    IPage<FsFileAuditLog> queryAllAuditLogs(int pageNum, int pageSize);
}

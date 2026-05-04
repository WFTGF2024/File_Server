package com.example.fileserver.modules.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.constant.FileConstants;
import com.example.fileserver.modules.audit.entity.FsFileAuditLog;
import com.example.fileserver.modules.audit.mapper.FileAuditLogMapper;
import com.example.fileserver.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final FileAuditLogMapper auditLogMapper;

    @Async
    @Override
    public void log(Long userId, String userType, String action, String resourceType,
                    Long resourceId, String resourceName, String detail,
                    String clientIp, String userAgent, int result, String errorMessage) {
        try {
            FsFileAuditLog auditLog = new FsFileAuditLog();
            auditLog.setUserId(userId);
            auditLog.setUserType(userType);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setResourceName(resourceName);
            auditLog.setDetail(detail);
            auditLog.setClientIp(clientIp);
            auditLog.setUserAgent(userAgent);
            auditLog.setResult(result);
            auditLog.setErrorMessage(errorMessage);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void log(Long userId, String userType, String action, String resourceType,
                    Long resourceId, String resourceName) {
        log(userId, userType, action, resourceType, resourceId, resourceName,
                "", "", "", FileConstants.AUDIT_RESULT_SUCCESS, "");
    }

    @Override
    public IPage<FsFileAuditLog> queryAuditLogs(int pageNum, int pageSize, Long userId, String action) {
        Page<FsFileAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFileAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, FsFileAuditLog::getUserId, userId)
               .eq(action != null && !action.isBlank(), FsFileAuditLog::getAction, action)
               .orderByDesc(FsFileAuditLog::getCreatedAt);
        return auditLogMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<FsFileAuditLog> queryAllAuditLogs(int pageNum, int pageSize) {
        Page<FsFileAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFileAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FsFileAuditLog::getCreatedAt);
        return auditLogMapper.selectPage(page, wrapper);
    }
}

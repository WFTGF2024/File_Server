package com.example.fileserver.modules.recycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.FileConstants;
import com.example.fileserver.common.enums.AuditActionEnum;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.audit.service.AuditService;
import com.example.fileserver.modules.file.entity.FsFile;
import com.example.fileserver.modules.file.service.FileService;
import com.example.fileserver.modules.quota.service.QuotaChecker;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;
import com.example.fileserver.modules.recycle.mapper.RecycleRecordMapper;
import com.example.fileserver.modules.recycle.service.RecycleService;
import com.example.fileserver.modules.folder.entity.FsFolder;
import com.example.fileserver.modules.folder.mapper.FolderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleServiceImpl implements RecycleService {

    private final RecycleRecordMapper recycleRecordMapper;
    private final FileService fileService;
    private final QuotaChecker quotaChecker;
    private final AuditService auditService;
    private final FolderMapper folderMapper;

    @Override
    public IPage<FsRecycleRecord> listRecycleRecords(Long userId, int pageNum, int pageSize) {
        Page<FsRecycleRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsRecycleRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsRecycleRecord::getUserId, userId)
               .orderByDesc(FsRecycleRecord::getDeletedAt);
        return recycleRecordMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void restoreFile(Long userId, Long recordId) {
        FsRecycleRecord record = recycleRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.RECYCLE_RECORD_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (FileConstants.RESOURCE_TYPE_FILE.equals(record.getResourceType())) {
            // Restore file
            FsFile file = fileService.getFileById(record.getResourceId());
            file.setIsDeleted(false);
            file.setDeletedAt(null);

            // Check if original parent folder still exists
            if (record.getOriginalParentId() != null && record.getOriginalParentId() > 0) {
                FsFolder parentFolder = folderMapper.selectById(record.getOriginalParentId());
                if (parentFolder == null || parentFolder.getIsDeleted()) {
                    // Parent folder no longer exists, restore to root
                    file.setParentFolderId(FileConstants.ROOT_FOLDER_ID_LONG);
                } else {
                    file.setParentFolderId(record.getOriginalParentId());
                }
            }

            // Use direct mapper update since we need to set isDeleted to false
            // MyBatis-Plus logic delete would interfere, so we use updateById with the entity
            // We need to handle this carefully since isDeleted is a logic delete field
            // For restore, we need to bypass the logic delete
            fileService.restoreFileRecord(file);
        } else if (FileConstants.RESOURCE_TYPE_FOLDER.equals(record.getResourceType())) {
            FsFolder folder = folderMapper.selectById(record.getResourceId());
            if (folder != null) {
                folder.setIsDeleted(false);
                folder.setDeletedAt(null);
                folderMapper.updateById(folder);
            }
        }

        // Delete recycle record
        recycleRecordMapper.deleteById(recordId);

        String userType = SecurityUserContext.getCurrentUserType();
        auditService.log(userId, userType, AuditActionEnum.RESTORE.getCode(),
                record.getResourceType(), record.getResourceId(), record.getResourceName());

        log.info("File restored: userId={}, recordId={}, resourceId={}", userId, recordId, record.getResourceId());
    }

    @Override
    @Transactional
    public void permanentDelete(Long userId, Long recordId) {
        FsRecycleRecord record = recycleRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.RECYCLE_RECORD_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (FileConstants.RESOURCE_TYPE_FILE.equals(record.getResourceType())) {
            FsFile file = fileService.getFileById(record.getResourceId());

            // Handle refCount and physical file
            if (file.getRefCount() > 1) {
                // Just decrement refCount
                file.setRefCount(file.getRefCount() - 1);
                // Need to use direct update since isDeleted is true
                fileService.decrementRefCount(file.getId());
            } else {
                // refCount is 1, delete physical file
                fileService.decrementRefCount(file.getId());
            }

            // Update quota
            quotaChecker.updateUsedStorage(userId, -record.getFileSize());
        }

        // Delete recycle record
        recycleRecordMapper.deleteById(recordId);

        String userType = SecurityUserContext.getCurrentUserType();
        auditService.log(userId, userType, AuditActionEnum.PERMANENT_DELETE.getCode(),
                record.getResourceType(), record.getResourceId(), record.getResourceName());

        log.info("File permanently deleted: userId={}, recordId={}, resourceId={}", userId, recordId, record.getResourceId());
    }

    @Override
    @Transactional
    public void cleanExpiredRecords() {
        LambdaQueryWrapper<FsRecycleRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(FsRecycleRecord::getExpireAt)
               .le(FsRecycleRecord::getExpireAt, LocalDateTime.now());
        List<FsRecycleRecord> expiredRecords = recycleRecordMapper.selectList(wrapper);

        log.info("Found {} expired recycle records to clean", expiredRecords.size());

        for (FsRecycleRecord record : expiredRecords) {
            try {
                if (FileConstants.RESOURCE_TYPE_FILE.equals(record.getResourceType())) {
                    FsFile file = fileService.getFileById(record.getResourceId());
                    if (file != null) {
                        if (file.getRefCount() <= 1) {
                            fileService.decrementRefCount(file.getId());
                        } else {
                            file.setRefCount(file.getRefCount() - 1);
                        }
                        quotaChecker.updateUsedStorage(record.getUserId(), -record.getFileSize());
                    }
                }
                recycleRecordMapper.deleteById(record.getId());
            } catch (Exception e) {
                log.error("Failed to clean expired record {}: {}", record.getId(), e.getMessage());
            }
        }

        log.info("Expired recycle records cleanup completed");
    }
}

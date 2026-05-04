package com.example.fileserver.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.FileConstants;
import com.example.fileserver.common.enums.AuditActionEnum;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.audit.entity.FsFileAuditLog;
import com.example.fileserver.modules.audit.service.AuditService;
import com.example.fileserver.modules.file.entity.FsFile;
import com.example.fileserver.modules.file.mapper.FileMapper;
import com.example.fileserver.modules.file.vo.FileVO;
import com.example.fileserver.modules.file.convert.FileConvert;
import com.example.fileserver.modules.quota.service.QuotaChecker;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;
import com.example.fileserver.modules.recycle.mapper.RecycleRecordMapper;
import com.example.fileserver.storage.adapter.StorageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileMapper fileMapper;
    private final AuditService auditService;
    private final QuotaChecker quotaChecker;
    private final StorageAdapter storageAdapter;
    private final RecycleRecordMapper recycleRecordMapper;

    @GetMapping("/users")
    public Result<IPage<FileVO>> getUsersFiles(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        checkAdmin();
        Page<FsFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFile::getOwnerUserId, userId)
               .eq(FsFile::getIsDeleted, false)
               .orderByDesc(FsFile::getCreatedAt);
        IPage<FsFile> filePage = fileMapper.selectPage(page, wrapper);
        return Result.success(filePage.convert(FileConvert::toVO));
    }

    @GetMapping("/user/{userId}")
    public Result<IPage<FileVO>> getUserFiles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        checkAdmin();
        Page<FsFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFile::getOwnerUserId, userId)
               .eq(FsFile::getIsDeleted, false)
               .orderByDesc(FsFile::getCreatedAt);
        IPage<FsFile> filePage = fileMapper.selectPage(page, wrapper);
        return Result.success(filePage.convert(FileConvert::toVO));
    }

    @DeleteMapping("/force-delete/{id}")
    public Result<Void> forceDelete(@PathVariable Long id) {
        checkAdmin();
        FsFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }

        // Delete physical file
        if (file.getRefCount() <= 1) {
            storageAdapter.delete(file.getStoragePath(), file.getStorageName());
        }

        // Delete file record
        fileMapper.deleteById(id);

        // Update quota
        if (!file.getIsDeleted()) {
            quotaChecker.updateUsedStorage(file.getOwnerUserId(), -file.getFileSize());
        }

        // Delete recycle record if exists
        LambdaQueryWrapper<FsRecycleRecord> recycleWrapper = new LambdaQueryWrapper<>();
        recycleWrapper.eq(FsRecycleRecord::getResourceId, id)
                      .eq(FsRecycleRecord::getResourceType, FileConstants.RESOURCE_TYPE_FILE);
        recycleRecordMapper.delete(recycleWrapper);

        // Audit log
        auditService.log(SecurityUserContext.getCurrentUserId(), SecurityUserContext.getCurrentUserType(),
                AuditActionEnum.FORCE_DELETE.getCode(), FileConstants.RESOURCE_TYPE_FILE, id, file.getFileName());

        log.info("Admin force deleted file: fileId={}, adminId={}", id, SecurityUserContext.getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/audit")
    public Result<IPage<FsFileAuditLog>> audit(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action) {
        checkAdmin();
        if (userId != null || action != null) {
            return Result.success(auditService.queryAuditLogs(pageNum, pageSize, userId, action));
        }
        return Result.success(auditService.queryAllAuditLogs(pageNum, pageSize));
    }

    @GetMapping("/system-storage")
    public Result<SystemStorageVO> systemStorage() {
        checkAdmin();

        Long totalFiles = fileMapper.selectCount(new LambdaQueryWrapper<FsFile>()
                .eq(FsFile::getIsDeleted, false));
        Long totalDeletedFiles = fileMapper.selectCount(new LambdaQueryWrapper<FsFile>()
                .eq(FsFile::getIsDeleted, true));

        SystemStorageVO vo = new SystemStorageVO();
        vo.setTotalFiles(totalFiles);
        vo.setTotalDeletedFiles(totalDeletedFiles);
        return Result.success(vo);
    }

    private void checkAdmin() {
        if (!SecurityUserContext.isAdmin()) {
            throw new BusinessException(ResultCode.ADMIN_ACCESS_DENIED);
        }
    }

    @lombok.Data
    public static class SystemStorageVO {
        private Long totalFiles;
        private Long totalDeletedFiles;
    }
}

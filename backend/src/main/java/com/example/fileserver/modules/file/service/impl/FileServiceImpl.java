package com.example.fileserver.modules.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.FileConstants;
import com.example.fileserver.common.enums.AuditActionEnum;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.exception.FileOperationException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.common.utils.FileHashUtil;
import com.example.fileserver.modules.audit.service.AuditService;
import com.example.fileserver.modules.file.convert.FileConvert;
import com.example.fileserver.modules.file.entity.FsFile;
import com.example.fileserver.modules.file.mapper.FileMapper;
import com.example.fileserver.modules.file.vo.FileDetailVO;
import com.example.fileserver.modules.file.vo.FileVO;
import com.example.fileserver.modules.file.service.FileService;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.service.PolicyService;
import com.example.fileserver.modules.quota.service.QuotaChecker;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;
import com.example.fileserver.modules.recycle.mapper.RecycleRecordMapper;
import com.example.fileserver.storage.adapter.StorageAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final RecycleRecordMapper recycleRecordMapper;
    private final StorageAdapter storageAdapter;
    private final QuotaChecker quotaChecker;
    private final PolicyService policyService;
    private final AuditService auditService;

    @Value("${file.storage.provider:local}")
    private String storageProvider;

    @Override
    @Transactional
    public FileVO upload(Long userId, MultipartFile file, Long parentFolderId, String description) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ResultCode.FILE_NAME_INVALID);
        }

        // Extract file extension
        String fileExt = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExt = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        // Check file type
        if (!quotaChecker.checkFileType(userId, fileExt)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }

        // Check single file size
        long fileSize = file.getSize();
        if (!quotaChecker.checkSingleFileSize(userId, fileSize)) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }

        // Check storage quota
        if (!quotaChecker.checkStorageQuota(userId, fileSize)) {
            throw new BusinessException(ResultCode.FILE_STORAGE_QUOTA_EXCEEDED);
        }

        // Compute file hash
        String fileHash;
        try {
            fileHash = FileHashUtil.computeSHA256(file);
        } catch (Exception e) {
            log.warn("Failed to compute file hash: {}", e.getMessage());
            fileHash = "";
        }

        // Check instant upload (hash dedup)
        String userType = SecurityUserContext.getCurrentUserType();
        FsUserCategoryPolicy policy = policyService.getPolicyByCategoryCode(userType);
        if (Boolean.TRUE.equals(policy.getAllowInstantUpload()) && !fileHash.isBlank()) {
            FsFile existingFile = findFileByHash(fileHash);
            if (existingFile != null) {
                // Instant upload: increment refCount, create new record
                FsFile newFile = new FsFile();
                newFile.setOwnerUserId(userId);
                newFile.setParentFolderId(parentFolderId != null ? parentFolderId : FileConstants.ROOT_FOLDER_ID_LONG);
                newFile.setFileName(originalFilename);
                newFile.setStorageName(existingFile.getStorageName());
                newFile.setFileExt(fileExt);
                newFile.setMimeType(file.getContentType());
                newFile.setFileSize(fileSize);
                newFile.setFileHash(fileHash);
                newFile.setStoragePath(existingFile.getStoragePath());
                newFile.setStorageProvider(existingFile.getStorageProvider());
                newFile.setRefCount(1);
                newFile.setIsDeleted(false);
                newFile.setPreviewStatus(FileConstants.PREVIEW_NOT_AVAILABLE);
                newFile.setDescription(description != null ? description : "");
                fileMapper.insert(newFile);

                // Increment refCount on existing file
                existingFile.setRefCount(existingFile.getRefCount() + 1);
                fileMapper.updateById(existingFile);

                quotaChecker.updateUsedStorage(userId, fileSize);
                auditService.log(userId, userType, AuditActionEnum.UPLOAD.getCode(),
                        FileConstants.RESOURCE_TYPE_FILE, newFile.getId(), originalFilename);

                log.info("Instant upload: userId={}, fileId={}, hash={}", userId, newFile.getId(), fileHash);
                return FileConvert.toVO(newFile);
            }
        }

        // Normal upload: store physical file
        String storageName = UUID.randomUUID().toString() + (fileExt.isEmpty() ? "" : "." + fileExt);
        String monthPath = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FileConstants.STORAGE_PATH_DATE_FORMAT));
        String storagePath = userId + FileConstants.PATH_SEPARATOR + monthPath;

        storageAdapter.store(file, storagePath, storageName);

        // Save file record
        FsFile fsFile = new FsFile();
        fsFile.setOwnerUserId(userId);
        fsFile.setParentFolderId(parentFolderId != null ? parentFolderId : FileConstants.ROOT_FOLDER_ID_LONG);
        fsFile.setFileName(originalFilename);
        fsFile.setStorageName(storageName);
        fsFile.setFileExt(fileExt);
        fsFile.setMimeType(file.getContentType());
        fsFile.setFileSize(fileSize);
        fsFile.setFileHash(fileHash);
        fsFile.setStoragePath(storagePath);
        fsFile.setStorageProvider(storageProvider);
        fsFile.setRefCount(1);
        fsFile.setIsDeleted(false);
        fsFile.setPreviewStatus(FileConstants.PREVIEW_NOT_AVAILABLE);
        fsFile.setDescription(description != null ? description : "");
        fileMapper.insert(fsFile);

        // Update quota
        quotaChecker.updateUsedStorage(userId, fileSize);

        // Audit log
        auditService.log(userId, userType, AuditActionEnum.UPLOAD.getCode(),
                FileConstants.RESOURCE_TYPE_FILE, fsFile.getId(), originalFilename);

        log.info("File uploaded: userId={}, fileId={}, name={}", userId, fsFile.getId(), originalFilename);
        return FileConvert.toVO(fsFile);
    }

    @Override
    public InputStream download(Long userId, Long fileId) {
        FsFile file = getFileById(fileId);
        if (!file.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (file.getIsDeleted()) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件已删除");
        }

        String userType = SecurityUserContext.getCurrentUserType();
        auditService.log(userId, userType, AuditActionEnum.DOWNLOAD.getCode(),
                FileConstants.RESOURCE_TYPE_FILE, fileId, file.getFileName());

        return storageAdapter.retrieve(file.getStoragePath(), file.getStorageName());
    }

    @Override
    public IPage<FileVO> listFiles(Long userId, Long parentFolderId, int pageNum, int pageSize) {
        Page<FsFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFile::getOwnerUserId, userId)
               .eq(FsFile::getParentFolderId, parentFolderId != null ? parentFolderId : FileConstants.ROOT_FOLDER_ID_LONG)
               .eq(FsFile::getIsDeleted, false)
               .orderByDesc(FsFile::getCreatedAt);

        IPage<FsFile> filePage = fileMapper.selectPage(page, wrapper);
        return filePage.convert(FileConvert::toVO);
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        FsFile file = getFileById(fileId);
        if (!file.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (file.getIsDeleted()) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件已删除");
        }

        // Logical delete
        file.setIsDeleted(true);
        file.setDeletedAt(LocalDateTime.now());
        fileMapper.updateById(file);

        // Create recycle record
        String userType = SecurityUserContext.getCurrentUserType();
        FsUserCategoryPolicy policy = policyService.getPolicyByCategoryCode(userType);

        FsRecycleRecord record = new FsRecycleRecord();
        record.setUserId(userId);
        record.setResourceType(FileConstants.RESOURCE_TYPE_FILE);
        record.setResourceId(fileId);
        record.setResourceName(file.getFileName());
        record.setOriginalParentId(file.getParentFolderId());
        record.setFileSize(file.getFileSize());
        record.setDeletedAt(LocalDateTime.now());
        record.setExpireAt(LocalDateTime.now().plusDays(policy.getRecycleRetentionDays()));
        recycleRecordMapper.insert(record);

        // Audit log
        auditService.log(userId, userType, AuditActionEnum.DELETE.getCode(),
                FileConstants.RESOURCE_TYPE_FILE, fileId, file.getFileName());

        log.info("File deleted (logical): userId={}, fileId={}", userId, fileId);
    }

    @Override
    public FileVO renameFile(Long userId, Long fileId, String newName) {
        FsFile file = getFileById(fileId);
        if (!file.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (file.getIsDeleted()) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件已删除");
        }

        // Validate new name
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ResultCode.FILE_NAME_INVALID);
        }
        if (newName.contains("..") || newName.contains("/") || newName.contains("\\")) {
            throw new BusinessException(ResultCode.FILE_NAME_INVALID, "文件名包含非法字符");
        }

        file.setFileName(newName);
        // Update extension if changed
        int dotIndex = newName.lastIndexOf('.');
        if (dotIndex > 0) {
            file.setFileExt(newName.substring(dotIndex + 1).toLowerCase());
        }
        fileMapper.updateById(file);

        String userType = SecurityUserContext.getCurrentUserType();
        auditService.log(userId, userType, AuditActionEnum.RENAME.getCode(),
                FileConstants.RESOURCE_TYPE_FILE, fileId, newName);

        return FileConvert.toVO(file);
    }

    @Override
    public FileVO moveFile(Long userId, Long fileId, Long targetFolderId) {
        FsFile file = getFileById(fileId);
        if (!file.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (file.getIsDeleted()) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "文件已删除");
        }

        file.setParentFolderId(targetFolderId != null ? targetFolderId : FileConstants.ROOT_FOLDER_ID_LONG);
        fileMapper.updateById(file);

        String userType = SecurityUserContext.getCurrentUserType();
        auditService.log(userId, userType, AuditActionEnum.MOVE.getCode(),
                FileConstants.RESOURCE_TYPE_FILE, fileId, file.getFileName());

        return FileConvert.toVO(file);
    }

    @Override
    public IPage<FileVO> searchFiles(Long userId, String keyword, int pageNum, int pageSize) {
        Page<FsFile> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFile::getOwnerUserId, userId)
               .eq(FsFile::getIsDeleted, false)
               .like(keyword != null && !keyword.isBlank(), FsFile::getFileName, keyword)
               .orderByDesc(FsFile::getCreatedAt);
        IPage<FsFile> filePage = fileMapper.selectPage(page, wrapper);
        return filePage.convert(FileConvert::toVO);
    }

    @Override
    public FileDetailVO getFileDetail(Long userId, Long fileId) {
        FsFile file = getFileById(fileId);
        if (!file.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return FileConvert.toDetailVO(file);
    }

    @Override
    public FsFile getFileById(Long fileId) {
        FsFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return file;
    }

    @Override
    public void decrementRefCount(Long fileId) {
        FsFile file = fileMapper.selectById(fileId);
        if (file != null && file.getRefCount() > 1) {
            file.setRefCount(file.getRefCount() - 1);
            fileMapper.updateById(file);
        } else if (file != null) {
            // refCount is 1, delete physical file
            storageAdapter.delete(file.getStoragePath(), file.getStorageName());
            fileMapper.deleteById(fileId);
        }
    }

    @Override
    public void deleteFileRecord(Long fileId) {
        fileMapper.deleteById(fileId);
    }

    @Override
    public void restoreFileRecord(FsFile file) {
        // Bypass logic delete by directly updating is_deleted to false
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<FsFile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.eq(FsFile::getId, file.getId())
               .set(FsFile::getIsDeleted, false)
               .set(FsFile::getDeletedAt, (LocalDateTime) null)
               .set(FsFile::getParentFolderId, file.getParentFolderId());
        fileMapper.update(null, wrapper);
    }

    private FsFile findFileByHash(String fileHash) {
        LambdaQueryWrapper<FsFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFile::getFileHash, fileHash)
               .eq(FsFile::getIsDeleted, false)
               .last("LIMIT 1");
        return fileMapper.selectOne(wrapper);
    }
}

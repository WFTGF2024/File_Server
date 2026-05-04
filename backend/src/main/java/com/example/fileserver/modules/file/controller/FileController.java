package com.example.fileserver.modules.file.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.file.dto.FileMoveDTO;
import com.example.fileserver.modules.file.dto.FileRenameDTO;
import com.example.fileserver.modules.file.vo.FileDetailVO;
import com.example.fileserver.modules.file.vo.FileVO;
import com.example.fileserver.modules.file.service.FileService;
import com.example.fileserver.modules.folder.service.FolderService;
import com.example.fileserver.modules.folder.vo.FolderTreeVO;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.service.PolicyService;
import com.example.fileserver.modules.quota.entity.FsUserQuota;
import com.example.fileserver.modules.quota.service.QuotaChecker;
import com.example.fileserver.modules.quota.vo.QuotaVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FolderService folderService;
    private final PolicyService policyService;
    private final QuotaChecker quotaChecker;

    @GetMapping("/home")
    public Result<FileHomeVO> home() {
        Long userId = SecurityUserContext.getCurrentUserId();
        String userType = SecurityUserContext.getCurrentUserType();

        List<FolderTreeVO> folderTree = folderService.getFolderTree(userId);
        FsUserCategoryPolicy policy = policyService.getPolicyByCategoryCode(userType);
        FsUserQuota quota = quotaChecker.getUserQuota(userId);

        QuotaVO quotaVO = new QuotaVO();
        quotaVO.setUserId(quota.getUserId());
        quotaVO.setTotalStorageLimitBytes(quota.getTotalStorageLimitBytes());
        quotaVO.setUsedStorageBytes(quota.getUsedStorageBytes());
        quotaVO.setMaxSingleFileSizeBytes(quota.getMaxSingleFileSizeBytes());
        quotaVO.setMaxBatchUploadCount(quota.getMaxBatchUploadCount());
        quotaVO.setPolicySource(quota.getPolicySource());
        if (quota.getTotalStorageLimitBytes() > 0) {
            quotaVO.setUsagePercent((double) quota.getUsedStorageBytes() / quota.getTotalStorageLimitBytes() * 100);
        } else {
            quotaVO.setUsagePercent(0.0);
        }

        FileHomeVO homeVO = new FileHomeVO();
        homeVO.setFolderTree(folderTree);
        homeVO.setPolicy(policy);
        homeVO.setQuota(quotaVO);
        return Result.success(homeVO);
    }

    @GetMapping("/list")
    public Result<IPage<FileVO>> list(
            @RequestParam(defaultValue = "0") Long parentFolderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(fileService.listFiles(userId, parentFolderId, pageNum, pageSize));
    }

    @PostMapping("/upload")
    public Result<FileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentFolderId", defaultValue = "0") Long parentFolderId,
            @RequestParam(value = "description", required = false) String description) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(fileService.upload(userId, file, parentFolderId, description));
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        Long userId = SecurityUserContext.getCurrentUserId();
        try {
            com.example.fileserver.modules.file.entity.FsFile file = fileService.getFileById(id);
            InputStream is = fileService.download(userId, id);

            response.setContentType(file.getMimeType() != null ? file.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + java.net.URLEncoder.encode(file.getFileName(), "UTF-8") + "\"");
            response.setContentLengthLong(file.getFileSize());

            try (OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) {
        Long userId = SecurityUserContext.getCurrentUserId();
        try {
            com.example.fileserver.modules.file.entity.FsFile file = fileService.getFileById(id);
            InputStream is = fileService.download(userId, id);

            String mimeType = file.getMimeType() != null ? file.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            response.setContentType(mimeType);
            // 预览模式使用 inline，浏览器会尝试内联显示
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + java.net.URLEncoder.encode(file.getFileName(), "UTF-8") + "\"");
            response.setContentLengthLong(file.getFileSize());

            try (OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Preview failed", e);
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUserContext.getCurrentUserId();
        fileService.deleteFile(userId, id);
        return Result.success();
    }

    @PutMapping("/rename")
    public Result<FileVO> rename(@Valid @RequestBody FileRenameDTO dto) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(fileService.renameFile(userId, dto.getFileId(), dto.getNewName()));
    }

    @PutMapping("/move")
    public Result<FileVO> move(@Valid @RequestBody FileMoveDTO dto) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(fileService.moveFile(userId, dto.getFileId(), dto.getTargetFolderId()));
    }

    @GetMapping("/search")
    public Result<IPage<FileVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(fileService.searchFiles(userId, keyword, pageNum, pageSize));
    }

    @GetMapping("/quota")
    public Result<QuotaVO> quota() {
        Long userId = SecurityUserContext.getCurrentUserId();
        FsUserQuota quota = quotaChecker.getUserQuota(userId);

        QuotaVO vo = new QuotaVO();
        vo.setUserId(quota.getUserId());
        vo.setTotalStorageLimitBytes(quota.getTotalStorageLimitBytes());
        vo.setUsedStorageBytes(quota.getUsedStorageBytes());
        vo.setMaxSingleFileSizeBytes(quota.getMaxSingleFileSizeBytes());
        vo.setMaxBatchUploadCount(quota.getMaxBatchUploadCount());
        vo.setPolicySource(quota.getPolicySource());
        if (quota.getTotalStorageLimitBytes() > 0) {
            vo.setUsagePercent((double) quota.getUsedStorageBytes() / quota.getTotalStorageLimitBytes() * 100);
        } else {
            vo.setUsagePercent(0.0);
        }
        return Result.success(vo);
    }

    @GetMapping("/policy")
    public Result<FsUserCategoryPolicy> policy() {
        String userType = SecurityUserContext.getCurrentUserType();
        return Result.success(policyService.getPolicyByCategoryCode(userType));
    }

    @lombok.Data
    public static class FileHomeVO {
        private List<FolderTreeVO> folderTree;
        private FsUserCategoryPolicy policy;
        private QuotaVO quota;
    }
}

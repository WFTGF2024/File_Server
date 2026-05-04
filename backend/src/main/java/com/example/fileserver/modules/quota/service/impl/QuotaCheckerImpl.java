package com.example.fileserver.modules.quota.service.impl;

import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.service.PolicyService;
import com.example.fileserver.modules.quota.entity.FsUserQuota;
import com.example.fileserver.modules.quota.service.QuotaChecker;
import com.example.fileserver.modules.quota.service.QuotaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaCheckerImpl implements QuotaChecker {

    private final QuotaService quotaService;
    private final PolicyService policyService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean checkStorageQuota(Long userId, long fileSize) {
        FsUserQuota quota = quotaService.getUserQuota(userId);
        long remaining = quota.getTotalStorageLimitBytes() - quota.getUsedStorageBytes();
        return remaining >= fileSize;
    }

    @Override
    public boolean checkSingleFileSize(Long userId, long fileSize) {
        FsUserQuota quota = quotaService.getUserQuota(userId);
        return fileSize <= quota.getMaxSingleFileSizeBytes();
    }

    @Override
    public boolean checkBatchUploadCount(Long userId, int count) {
        FsUserQuota quota = quotaService.getUserQuota(userId);
        return count <= quota.getMaxBatchUploadCount();
    }

    @Override
    public boolean checkFileType(Long userId, String fileExt) {
        String userType = SecurityUserContext.getCurrentUserType();
        FsUserCategoryPolicy policy = policyService.getPolicyByCategoryCode(userType);

        String allowedTypes = policy.getAllowedFileTypes();
        // Empty array means all types allowed (ADMIN)
        if (allowedTypes == null || allowedTypes.equals("[]") || allowedTypes.isBlank()) {
            return true;
        }

        try {
            JsonNode array = objectMapper.readTree(allowedTypes);
            if (array.isArray()) {
                List<String> types = objectMapper.convertValue(array, List.class);
                if (types.isEmpty()) {
                    return true;
                }
                String extWithDot = fileExt.startsWith(".") ? fileExt : "." + fileExt;
                return types.contains(extWithDot.toLowerCase());
            }
        } catch (Exception e) {
            log.warn("Failed to parse allowed file types: {}", allowedTypes);
        }
        return true;
    }

    @Override
    public FsUserQuota getUserQuota(Long userId) {
        return quotaService.getUserQuota(userId);
    }

    @Override
    public void updateUsedStorage(Long userId, long deltaBytes) {
        quotaService.updateUsedStorage(userId, deltaBytes);
    }
}

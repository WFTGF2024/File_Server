package com.example.fileserver.modules.quota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.PolicyConstants;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.service.PolicyService;
import com.example.fileserver.modules.quota.entity.FsUserQuota;
import com.example.fileserver.modules.quota.mapper.UserQuotaMapper;
import com.example.fileserver.modules.quota.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final UserQuotaMapper quotaMapper;
    private final PolicyService policyService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long QUOTA_CACHE_TTL_MINUTES = 5;

    @Override
    public FsUserQuota getUserQuota(Long userId) {
        String cacheKey = PolicyConstants.CACHE_QUOTA_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof FsUserQuota quota) {
            return quota;
        }

        LambdaQueryWrapper<FsUserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsUserQuota::getUserId, userId);
        FsUserQuota quota = quotaMapper.selectOne(wrapper);

        if (quota == null) {
            quota = createQuotaFromPolicy(userId);
        }

        redisTemplate.opsForValue().set(cacheKey, quota, QUOTA_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return quota;
    }

    @Override
    public void updateUsedStorage(Long userId, long deltaBytes) {
        LambdaUpdateWrapper<FsUserQuota> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FsUserQuota::getUserId, userId)
               .setSql("used_storage_bytes = used_storage_bytes + " + deltaBytes);
        quotaMapper.update(null, wrapper);

        // Clear quota cache
        String cacheKey = PolicyConstants.CACHE_QUOTA_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }

    @Override
    public void updateUserQuota(Long userId, FsUserQuota quota) {
        LambdaQueryWrapper<FsUserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsUserQuota::getUserId, userId);
        FsUserQuota existing = quotaMapper.selectOne(wrapper);

        if (existing == null) {
            quota.setUserId(userId);
            quota.setPolicySource(PolicyConstants.POLICY_SOURCE_CUSTOM);
            quotaMapper.insert(quota);
        } else {
            existing.setTotalStorageLimitBytes(quota.getTotalStorageLimitBytes());
            existing.setMaxSingleFileSizeBytes(quota.getMaxSingleFileSizeBytes());
            existing.setMaxBatchUploadCount(quota.getMaxBatchUploadCount());
            existing.setPolicySource(PolicyConstants.POLICY_SOURCE_CUSTOM);
            quotaMapper.updateById(existing);
        }

        // Clear cache
        String cacheKey = PolicyConstants.CACHE_QUOTA_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }

    private FsUserQuota createQuotaFromPolicy(Long userId) {
        String userType = SecurityUserContext.getCurrentUserType();
        FsUserCategoryPolicy policy = policyService.getPolicyByCategoryCode(userType);

        FsUserQuota quota = new FsUserQuota();
        quota.setUserId(userId);
        quota.setTotalStorageLimitBytes(policy.getTotalStorageLimitBytes());
        quota.setUsedStorageBytes(0L);
        quota.setMaxSingleFileSizeBytes(policy.getMaxSingleFileSizeBytes());
        quota.setMaxBatchUploadCount(policy.getMaxBatchUploadCount());
        quota.setPolicySource(PolicyConstants.POLICY_SOURCE_CATEGORY);
        quota.setStatus(PolicyConstants.STATUS_ENABLED);
        quotaMapper.insert(quota);

        log.info("Created quota for user {} from policy {}", userId, userType);
        return quota;
    }
}

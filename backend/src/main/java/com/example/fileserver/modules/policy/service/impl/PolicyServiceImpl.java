package com.example.fileserver.modules.policy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.PolicyConstants;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.mapper.UserCategoryPolicyMapper;
import com.example.fileserver.modules.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final UserCategoryPolicyMapper policyMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.policy.ttl:30}")
    private long policyCacheTtl;

    @Override
    public FsUserCategoryPolicy getPolicyByCategoryCode(String categoryCode) {
        String cacheKey = PolicyConstants.CACHE_POLICY_PREFIX + categoryCode;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof FsUserCategoryPolicy policy) {
            return policy;
        }

        LambdaQueryWrapper<FsUserCategoryPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsUserCategoryPolicy::getCategoryCode, categoryCode)
               .eq(FsUserCategoryPolicy::getStatus, PolicyConstants.STATUS_ENABLED);
        FsUserCategoryPolicy policy = policyMapper.selectOne(wrapper);

        if (policy == null) {
            throw new BusinessException(ResultCode.POLICY_NOT_FOUND, "策略不存在: " + categoryCode);
        }

        redisTemplate.opsForValue().set(cacheKey, policy, policyCacheTtl, TimeUnit.MINUTES);
        return policy;
    }

    @Override
    public FsUserCategoryPolicy getPolicyByUserId(Long userId) {
        String userType = SecurityUserContext.getCurrentUserType();
        return getPolicyByCategoryCode(userType);
    }

    @Override
    public List<FsUserCategoryPolicy> getAllPolicies() {
        LambdaQueryWrapper<FsUserCategoryPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FsUserCategoryPolicy::getId);
        return policyMapper.selectList(wrapper);
    }

    @Override
    public void updatePolicy(FsUserCategoryPolicy policy) {
        if (policy.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "策略ID不能为空");
        }

        FsUserCategoryPolicy existing = policyMapper.selectById(policy.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.POLICY_NOT_FOUND);
        }

        policyMapper.updateById(policy);

        // Clear cache
        String cacheKey = PolicyConstants.CACHE_POLICY_PREFIX + existing.getCategoryCode();
        redisTemplate.delete(cacheKey);
        log.info("Policy updated and cache cleared: {}", existing.getCategoryCode());
    }

    @Override
    public void initDefaultPolicies() {
        LambdaQueryWrapper<FsUserCategoryPolicy> wrapper = new LambdaQueryWrapper<>();
        Long count = policyMapper.selectCount(wrapper);
        if (count > 0) {
            log.info("Policies already initialized, skip");
            return;
        }
        log.info("No policies found, please run SQL init script first");
    }
}

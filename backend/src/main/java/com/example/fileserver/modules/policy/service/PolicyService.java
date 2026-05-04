package com.example.fileserver.modules.policy.service;

import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;

import java.util.List;

public interface PolicyService {

    FsUserCategoryPolicy getPolicyByCategoryCode(String categoryCode);

    FsUserCategoryPolicy getPolicyByUserId(Long userId);

    List<FsUserCategoryPolicy> getAllPolicies();

    void updatePolicy(FsUserCategoryPolicy policy);

    void initDefaultPolicies();
}

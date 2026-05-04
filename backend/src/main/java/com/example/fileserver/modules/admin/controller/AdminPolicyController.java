package com.example.fileserver.modules.admin.controller;

import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import com.example.fileserver.modules.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/policies")
@RequiredArgsConstructor
public class AdminPolicyController {

    private final PolicyService policyService;

    @GetMapping("/list")
    public Result<List<FsUserCategoryPolicy>> list() {
        checkAdmin();
        return Result.success(policyService.getAllPolicies());
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody FsUserCategoryPolicy policy) {
        checkAdmin();
        policyService.updatePolicy(policy);
        return Result.success();
    }

    private void checkAdmin() {
        if (!SecurityUserContext.isAdmin()) {
            throw new BusinessException(ResultCode.ADMIN_ACCESS_DENIED);
        }
    }
}

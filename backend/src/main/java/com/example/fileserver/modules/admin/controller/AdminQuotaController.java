package com.example.fileserver.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.quota.entity.FsUserQuota;
import com.example.fileserver.modules.quota.mapper.UserQuotaMapper;
import com.example.fileserver.modules.quota.service.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quotas")
@RequiredArgsConstructor
public class AdminQuotaController {

    private final QuotaService quotaService;
    private final UserQuotaMapper quotaMapper;

    @GetMapping("/list")
    public Result<IPage<FsUserQuota>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        checkAdmin();
        Page<FsUserQuota> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsUserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FsUserQuota::getUsedStorageBytes);
        return Result.success(quotaMapper.selectPage(page, wrapper));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody FsUserQuota quota) {
        checkAdmin();
        quotaService.updateUserQuota(quota.getUserId(), quota);
        return Result.success();
    }

    private void checkAdmin() {
        if (!SecurityUserContext.isAdmin()) {
            throw new BusinessException(ResultCode.ADMIN_ACCESS_DENIED);
        }
    }
}

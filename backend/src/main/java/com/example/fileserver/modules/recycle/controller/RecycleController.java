package com.example.fileserver.modules.recycle.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;
import com.example.fileserver.modules.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recycle")
@RequiredArgsConstructor
public class RecycleController {

    private final RecycleService recycleService;

    @GetMapping("/list")
    public Result<IPage<FsRecycleRecord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(recycleService.listRecycleRecords(userId, pageNum, pageSize));
    }

    @PostMapping("/restore/{id}")
    public Result<Void> restore(@PathVariable Long id) {
        Long userId = SecurityUserContext.getCurrentUserId();
        recycleService.restoreFile(userId, id);
        return Result.success();
    }

    @DeleteMapping("/remove/{id}")
    public Result<Void> permanentDelete(@PathVariable Long id) {
        Long userId = SecurityUserContext.getCurrentUserId();
        recycleService.permanentDelete(userId, id);
        return Result.success();
    }
}

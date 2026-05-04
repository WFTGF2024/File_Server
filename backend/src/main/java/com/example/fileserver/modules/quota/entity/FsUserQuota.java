package com.example.fileserver.modules.quota.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_user_quota")
public class FsUserQuota {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long totalStorageLimitBytes;

    private Long usedStorageBytes;

    private Long maxSingleFileSizeBytes;

    private Integer maxBatchUploadCount;

    private String policySource;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

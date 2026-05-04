package com.example.fileserver.modules.recycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_recycle_record")
public class FsRecycleRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String resourceType;

    private Long resourceId;

    private String resourceName;

    private Long originalParentId;

    private Long fileSize;

    private LocalDateTime deletedAt;

    private LocalDateTime expireAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

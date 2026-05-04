package com.example.fileserver.modules.quota.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fs_storage_stat")
public class FsStorageStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String statType;

    private Long targetId;

    private Long totalFiles;

    private Long totalFolders;

    private Long totalSize;

    private Integer totalRefCount;

    private LocalDate statDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

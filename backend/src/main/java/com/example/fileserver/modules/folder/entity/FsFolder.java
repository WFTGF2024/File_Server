package com.example.fileserver.modules.folder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_folder")
public class FsFolder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerUserId;

    private Long parentId;

    private String folderName;

    private String folderPath;

    private Integer depth;

    private Integer sortOrder;

    @TableField(value = "is_deleted")
    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

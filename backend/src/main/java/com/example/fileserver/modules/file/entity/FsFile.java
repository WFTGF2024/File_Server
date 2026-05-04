package com.example.fileserver.modules.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_file")
public class FsFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ownerUserId;

    private Long parentFolderId;

    private String fileName;

    private String storageName;

    private String fileExt;

    private String mimeType;

    private Long fileSize;

    private String fileHash;

    private String storagePath;

    private String storageProvider;

    private Integer refCount;

    @TableField(value = "is_deleted")
    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    private Integer previewStatus;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

package com.example.fileserver.modules.policy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_user_category_policy")
public class FsUserCategoryPolicy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryCode;

    private String categoryName;

    private Long totalStorageLimitBytes;

    private Long maxSingleFileSizeBytes;

    private Integer maxBatchUploadCount;

    private String allowedFileTypes;

    private Boolean allowChunkUpload;

    private Boolean allowInstantUpload;

    private Boolean allowPreview;

    private Boolean allowShareLink;

    private Boolean allowSharePassword;

    private Boolean allowShareExpire;

    private Boolean allowFolderShare;

    private Boolean allowVersioning;

    private Integer recycleRetentionDays;

    private Boolean allowOverwriteUpload;

    private Boolean allowApiUpload;

    private Boolean allowTeamSharedFolder;

    private Boolean downloadPriority;

    private Boolean allowExpansionApply;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

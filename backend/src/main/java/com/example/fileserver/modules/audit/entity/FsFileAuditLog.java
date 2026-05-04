package com.example.fileserver.modules.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fs_file_audit_log")
public class FsFileAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userType;

    private String action;

    private String resourceType;

    private Long resourceId;

    private String resourceName;

    private String detail;

    private String clientIp;

    private String userAgent;

    private Integer result;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

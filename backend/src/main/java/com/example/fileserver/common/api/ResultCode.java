package com.example.fileserver.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或Token无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 文件相关 1xxx
    FILE_NOT_FOUND(1001, "文件不存在"),
    FILE_ALREADY_EXISTS(1002, "文件已存在"),
    FILE_UPLOAD_FAILED(1003, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(1004, "文件下载失败"),
    FILE_DELETE_FAILED(1005, "文件删除失败"),
    FILE_SIZE_EXCEEDED(1006, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(1007, "文件类型不允许"),
    FILE_STORAGE_QUOTA_EXCEEDED(1008, "存储空间不足"),
    FILE_BATCH_COUNT_EXCEEDED(1009, "批量上传数量超出限制"),
    FILE_NAME_INVALID(1010, "文件名不合法"),
    FILE_HASH_MISMATCH(1011, "文件哈希校验失败"),
    FILE_OPERATION_FAILED(1012, "文件操作失败"),

    // 文件夹相关 2xxx
    FOLDER_NOT_FOUND(2001, "文件夹不存在"),
    FOLDER_ALREADY_EXISTS(2002, "同名文件夹已存在"),
    FOLDER_DEPTH_EXCEEDED(2003, "文件夹深度超出限制"),
    FOLDER_NAME_INVALID(2004, "文件夹名不合法"),

    // 策略相关 3xxx
    POLICY_NOT_FOUND(3001, "策略不存在"),
    POLICY_LOAD_FAILED(3002, "策略加载失败"),

    // 配额相关 4xxx
    QUOTA_NOT_FOUND(4001, "配额记录不存在"),
    QUOTA_INSUFFICIENT(4002, "配额不足"),

    // 回收站相关 5xxx
    RECYCLE_RECORD_NOT_FOUND(5001, "回收站记录不存在"),
    RECYCLE_RESTORE_FAILED(5002, "恢复失败"),

    // 认证相关 6xxx
    AUTH_TOKEN_INVALID(6001, "Token无效"),
    AUTH_TOKEN_EXPIRED(6002, "Token已过期"),
    AUTH_USER_INFO_FAILED(6003, "获取用户信息失败"),
    AUTH_BACKEND_UNAVAILABLE(6004, "认证服务不可用"),

    // 管理端相关 7xxx
    ADMIN_ACCESS_DENIED(7001, "管理员权限不足"),
    ADMIN_OPERATION_FAILED(7002, "管理操作失败");

    private final int code;
    private final String message;
}

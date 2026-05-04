package com.example.fileserver.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditActionEnum {

    UPLOAD("upload", "上传"),
    DOWNLOAD("download", "下载"),
    DELETE("delete", "删除"),
    RENAME("rename", "重命名"),
    MOVE("move", "移动"),
    SHARE("share", "分享"),
    RESTORE("restore", "恢复"),
    FORCE_DELETE("force_delete", "强制删除"),
    PERMANENT_DELETE("permanent_delete", "永久删除"),
    CREATE_FOLDER("create_folder", "创建文件夹"),
    DELETE_FOLDER("delete_folder", "删除文件夹");

    private final String code;
    private final String name;
}

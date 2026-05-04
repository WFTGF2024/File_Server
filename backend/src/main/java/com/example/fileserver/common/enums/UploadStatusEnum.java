package com.example.fileserver.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadStatusEnum {

    INIT(0, "初始化"),
    UPLOADING(1, "上传中"),
    MERGING(2, "合并中"),
    COMPLETED(3, "已完成"),
    FAILED(4, "失败"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String name;
}

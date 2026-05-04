package com.example.fileserver.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageProviderEnum {

    LOCAL("local", "本地存储"),
    MINIO("minio", "MinIO存储");

    private final String code;
    private final String name;
}

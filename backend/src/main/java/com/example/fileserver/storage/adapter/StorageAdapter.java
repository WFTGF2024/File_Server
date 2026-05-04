package com.example.fileserver.storage.adapter;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageAdapter {

    String store(MultipartFile file, String storagePath, String storageName);

    InputStream retrieve(String storagePath, String storageName);

    boolean delete(String storagePath, String storageName);

    boolean exists(String storagePath, String storageName);

    long getSize(String storagePath, String storageName);

    String getProviderName();
}

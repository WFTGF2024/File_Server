package com.example.fileserver.storage.local;

import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.exception.FileOperationException;
import com.example.fileserver.storage.adapter.StorageAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class LocalStorageAdapter implements StorageAdapter {

    @Value("${file.storage.local.base-path}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String storagePath, String storageName) {
        try {
            Path dirPath = Paths.get(basePath, storagePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(storageName);
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored: {}/{}", storagePath, storageName);
            return storageName;
        } catch (IOException e) {
            log.error("Failed to store file: {}/{}", storagePath, storageName, e);
            throw new FileOperationException(ResultCode.FILE_UPLOAD_FAILED, "文件存储失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieve(String storagePath, String storageName) {
        try {
            Path filePath = Paths.get(basePath, storagePath, storageName);
            if (!Files.exists(filePath)) {
                throw new FileOperationException(ResultCode.FILE_NOT_FOUND, "文件不存在: " + storageName);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("Failed to retrieve file: {}/{}", storagePath, storageName, e);
            throw new FileOperationException(ResultCode.FILE_DOWNLOAD_FAILED, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String storagePath, String storageName) {
        try {
            Path filePath = Paths.get(basePath, storagePath, storageName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}/{}", storagePath, storageName);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to delete file: {}/{}", storagePath, storageName, e);
            return false;
        }
    }

    @Override
    public boolean exists(String storagePath, String storageName) {
        Path filePath = Paths.get(basePath, storagePath, storageName);
        return Files.exists(filePath);
    }

    @Override
    public long getSize(String storagePath, String storageName) {
        try {
            Path filePath = Paths.get(basePath, storagePath, storageName);
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
            return 0;
        } catch (IOException e) {
            log.error("Failed to get file size: {}/{}", storagePath, storageName, e);
            return 0;
        }
    }

    @Override
    public String getProviderName() {
        return "local";
    }
}

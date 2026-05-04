package com.example.fileserver.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashUtil {

    private static final int BUFFER_SIZE = 8192;
    private static final String HASH_ALGORITHM = "SHA-256";

    private FileHashUtil() {
    }

    public static String computeSHA256(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return computeSHA256(is);
        }
    }

    public static String computeSHA256(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

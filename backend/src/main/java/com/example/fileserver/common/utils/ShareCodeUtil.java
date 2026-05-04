package com.example.fileserver.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class ShareCodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ShareCodeUtil() {
    }

    public static String generateShareCode() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

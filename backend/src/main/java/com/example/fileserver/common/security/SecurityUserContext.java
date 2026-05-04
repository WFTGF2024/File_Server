package com.example.fileserver.common.security;

import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUserContext {

    private SecurityUserContext() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    public static String getCurrentUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String userType) {
            return userType;
        }
        return "NORMAL";
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserType());
    }
}

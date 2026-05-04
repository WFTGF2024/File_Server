package com.example.fileserver.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserBackendAuthClient userBackendAuthClient;

    public JwtAuthenticationFilter(UserBackendAuthClient userBackendAuthClient) {
        this.userBackendAuthClient = userBackendAuthClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                boolean valid = userBackendAuthClient.validateToken(token);
                if (valid) {
                    UserBackendAuthClient.UserInfoVO userInfo = userBackendAuthClient.getUserInfo(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userInfo.userId(), null, new ArrayList<>());
                    authentication.setDetails(userInfo.userType());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    request.setAttribute("currentUserId", userInfo.userId());
                    request.setAttribute("currentUserType", userInfo.userType());
                    request.setAttribute("currentAccount", userInfo.account());
                    request.setAttribute("currentNickname", userInfo.nickname());
                }
            } catch (Exception e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

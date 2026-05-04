package com.example.fileserver.common.security;

import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class UserBackendAuthClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user-backend.base-url}")
    private String baseUrl;

    @Value("${user-backend.validate-url}")
    private String validateUrl;

    @Value("${user-backend.user-info-url}")
    private String userInfoUrl;

    public UserBackendAuthClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + validateUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("data").asBoolean(false);
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to validate token via user_backend: {}", e.getMessage());
            throw new BusinessException(ResultCode.AUTH_BACKEND_UNAVAILABLE);
        }
    }

    public UserInfoVO getUserInfo(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");
                return new UserInfoVO(
                        data.path("userId").asLong(),
                        data.path("account").asText(""),
                        data.path("nickname").asText(""),
                        data.path("userType").asText("NORMAL")
                );
            }
            throw new BusinessException(ResultCode.AUTH_USER_INFO_FAILED);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get user info via user_backend: {}", e.getMessage());
            throw new BusinessException(ResultCode.AUTH_USER_INFO_FAILED);
        }
    }

    public record UserInfoVO(Long userId, String account, String nickname, String userType) {
    }
}

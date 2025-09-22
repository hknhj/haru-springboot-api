package com.haru.api.auth.application.service;

import com.haru.api.auth.application.port.in.TokenUseCase;
import com.haru.api.auth.application.port.out.TokenPort;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.infra.security.jwt.SecurityUtil;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.REFRESH_TOKEN_NOT_EQUAL;

@Component
@RequiredArgsConstructor
public class TokenUseCaseImpl implements TokenUseCase {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenPort tokenPort;

    @Override
    public UserResponseDTO.RefreshResponse refresh(String refreshToken) {
        Long userId = SecurityUtil.getCurrentUserId();
        String key = "users:" + userId;
        String accessToken;
        String newRefreshToken;

        // 전달된 refresh token과 redis의 refresh token비교
        String getRefreshTokenFromRedis = redisTemplate.opsForValue().get(key);
        System.out.println("userId: " + userId);
        System.out.println("redis에서 가져온 refreshToken: " + getRefreshTokenFromRedis);
        if (refreshToken.equals(getRefreshTokenFromRedis)) {
            accessToken = tokenPort.generateAccessToken(userId);
            newRefreshToken = tokenPort.generateAndSaveRefreshToken(userId);
        } else {
            throw new MemberHandler(REFRESH_TOKEN_NOT_EQUAL);
        }

        return UserConverter.toRefreshResponse(userId, accessToken, newRefreshToken);
    }
}

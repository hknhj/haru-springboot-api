package com.haru.api.auth.application.service;

import com.haru.api.auth.application.port.in.LogoutUseCase;
import com.haru.api.auth.application.port.out.TokenPort;
import com.haru.api.infra.security.jwt.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenPort tokenPort;

    @Override
    public void logout(String accessToken) {

        // 로그아웃시킬 회원의 refresh token redis에서 삭제
        Long userId = SecurityUtil.getCurrentUserId();
        String key = "users:" + userId;
        redisTemplate.delete(key);

        // 로그아웃시킬 회원의 access token redis의 블랙리스트로 저장, 인가 처리시 블랙리스트 확인을 통해 로그아웃된 회원인지 확인함.
        key = "blackList:" + userId;
        long tokenRemainTimeSecond = tokenPort.tokenRemainTimeSecond(accessToken);
        redisTemplate.opsForValue().set(key, accessToken, tokenRemainTimeSecond, TimeUnit.SECONDS);
    }
}

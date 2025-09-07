package com.haru.api.user.infrastructure.adapter;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.infra.security.jwt.JwtUtils;
import com.haru.api.infra.security.jwt.SecurityUtil;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.application.port.out.AuthPort;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.REFRESH_TOKEN_NOT_EQUAL;

@Service
@RequiredArgsConstructor
public class AuthImpl implements AuthPort {

    private final UserPort userPort;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // jwt토큰(access token, refresh token) 생성
        User getUser = userPort.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String key = "users:" + getUser.getId().toString();
        String accessToken = jwtUtils.generateAccessToken(getUser.getId());
        String refreshToken = jwtUtils.generateAndSaveRefreshToken(key);

        return UserConverter.toLoginResponse(getUser, accessToken, refreshToken);
    }

    @Override
    public void logout(String accessToken) {

        // 로그아웃시킬 회원의 refresh token redis에서 삭제
        Long userId = SecurityUtil.getCurrentUserId();
        String key = "users:" + userId;
        redisTemplate.delete(key);

        // 로그아웃시킬 회원의 access token redis의 블랙리스트로 저장, 인가 처리시 블랙리스트 확인을 통해 로그아웃된 회원인지 확인함.
        key = "blackList:" + userId;
        long tokenRemainTimeSecond = jwtUtils.tokenRemainTimeSecond(accessToken);
        redisTemplate.opsForValue().set(key, accessToken, tokenRemainTimeSecond, TimeUnit.SECONDS);
    }

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
            accessToken = jwtUtils.generateAccessToken(userId);
            newRefreshToken = jwtUtils.generateAndSaveRefreshToken(key);
        } else {
            throw new MemberHandler(REFRESH_TOKEN_NOT_EQUAL);
        }

        return UserConverter.toRefreshResponse(userId, accessToken, newRefreshToken);

    }
}

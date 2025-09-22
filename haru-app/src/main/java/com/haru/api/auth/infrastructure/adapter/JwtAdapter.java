package com.haru.api.auth.infrastructure.adapter;

import com.haru.api.auth.application.port.out.TokenPort;
import com.haru.api.infra.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAdapter implements TokenPort {

    private final JwtUtils jwtUtils;


    @Override
    public String generateAccessToken(Long userId) {
        return jwtUtils.generateAccessToken(userId);
    }

    @Override
    public String generateAndSaveRefreshToken(Long userId) {
        String key = "users:" + userId;
        return jwtUtils.generateAndSaveRefreshToken(key);
    }

    @Override
    public long tokenRemainTimeSecond(String header) { // static 제거
        return jwtUtils.tokenRemainTimeSecond(header);
    }
}

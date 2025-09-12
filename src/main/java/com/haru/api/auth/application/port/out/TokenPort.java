package com.haru.api.auth.application.port.out;

public interface TokenPort {

    String generateAccessToken(Long userId);

    String generateAndSaveRefreshToken(Long userId);

    long tokenRemainTimeSecond(String header);
}

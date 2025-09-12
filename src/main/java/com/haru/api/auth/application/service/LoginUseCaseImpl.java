package com.haru.api.auth.application.service;

import com.haru.api.auth.application.port.in.LoginUseCase;
import com.haru.api.auth.application.port.out.AuthUserPort;
import com.haru.api.auth.application.port.out.AuthenticatePort;
import com.haru.api.auth.application.port.out.TokenPort;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthenticatePort authenticatePort;
    private final AuthUserPort authUserPort;
    private final TokenPort tokenPort;

    @Override
    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {

        Authentication authentication = authenticatePort.authenticate(request.getEmail(), request.getPassword());

        User getUser = authUserPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String accessToken = tokenPort.generateAccessToken(getUser.getId());
        String refreshToken = tokenPort.generateAndSaveRefreshToken(getUser.getId());

        return UserConverter.toLoginResponse(getUser, accessToken, refreshToken);
    }
}

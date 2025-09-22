package com.haru.api.auth.application.facade;

import com.haru.api.auth.application.port.in.LoginUseCase;
import com.haru.api.auth.application.port.in.LogoutUseCase;
import com.haru.api.auth.application.port.in.TokenUseCase;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final TokenUseCase tokenUseCase;

    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {
        return loginUseCase.login(request);
    }

    public void logout(String accessToken) {
        logoutUseCase.logout(accessToken);
    }

    public UserResponseDTO.RefreshResponse refresh(String refreshToken) {
        return tokenUseCase.refresh(refreshToken);
    }
}

package com.haru.api.user.application.port.out;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;

public interface AuthPort {

    UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request);

    void logout(String accessToken);

    UserResponseDTO.RefreshResponse refresh(String accessToken);
}

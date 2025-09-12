package com.haru.api.auth.application.port.in;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;

public interface LoginUseCase {

    UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request);

}

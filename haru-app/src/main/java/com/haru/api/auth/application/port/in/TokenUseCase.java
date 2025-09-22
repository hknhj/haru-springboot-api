package com.haru.api.auth.application.port.in;

import com.haru.api.user.presentation.dto.UserResponseDTO;

public interface TokenUseCase {

    UserResponseDTO.RefreshResponse refresh(String refreshToken);

}

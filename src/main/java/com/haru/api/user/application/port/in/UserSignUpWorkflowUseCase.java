package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;

public interface UserSignUpWorkflowUseCase {

    UserResponseDTO.User signUp(UserRequestDTO.SignUpRequest request, String token);

    UserResponseDTO.LoginResponse signUpAndLogin(UserRequestDTO.SignUpRequest request, String token);

}
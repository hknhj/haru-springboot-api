package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;

public interface UserCommandUseCase {

    UserResponseDTO.User signUp(UserRequestDTO.SignUpRequest request, String token);

    UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request);

    UserResponseDTO.User updateUserInfo(User user, UserRequestDTO.UserInfoUpdateRequest request);

    UserResponseDTO.RefreshResponse refresh(String refreshToken);

    void logout(String accessToken);

    UserResponseDTO.CheckEmailDuplicationResponse checkEmailDuplication(UserRequestDTO.CheckEmailDuplicationRequest request);

    UserResponseDTO.CheckOriginalPasswordResponse checkOriginalPassword(UserRequestDTO.CheckOriginalPasswordRequest request, User user);

    User createUser(UserRequestDTO.SignUpRequest request);
}

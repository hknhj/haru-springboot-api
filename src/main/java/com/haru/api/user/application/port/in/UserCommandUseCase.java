package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;

public interface UserCommandUseCase {
    User signUp(UserRequestDTO.SignUpRequest request);
    UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request);

    UserResponseDTO.User updateUserInfo(User user, UserRequestDTO.UserInfoUpdateRequest request);

    UserResponseDTO.RefreshResponse refresh(String refreshToken);

    void logout(String accessToken);

    String generateAccessToken(Long userId, int accessExpTime);

    String generateAndSaveRefreshToken(String key, int refreshExpTime);

    UserResponseDTO.CheckEmailDuplicationResponse checkEmailDuplication(UserRequestDTO.CheckEmailDuplicationRequest request);

    UserResponseDTO.CheckOriginalPasswordResponse checkOriginalPassword(UserRequestDTO.CheckOriginalPasswordRequest request, User user);

    UserResponseDTO.LoginResponse signupAndLoginAndInviteAccept(UserRequestDTO.SignUpRequest request, String token);
}

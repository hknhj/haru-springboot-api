package com.haru.api.user.application.service;

import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.application.port.in.UserCommandUseCase;
import com.haru.api.user.application.port.in.UserSignUpWorkflowUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.workspace.application.port.in.WorkspaceCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSignUpWorkflowUseCaseImpl implements UserSignUpWorkflowUseCase {

    private final UserCommandUseCase userCommandUseCase;
    private final WorkspaceCommandUseCase workspaceCommandUseCase;

    @Override
    public UserResponseDTO.LoginResponse signUpAndLogin(UserRequestDTO.SignUpRequest request, String token) {

        // 회원가입
        User createdUser = userCommandUseCase.createUser(request);

        // 초대 토큰이 있는 경우, 초대 수락 로직 호출
        if (token != null) {
            workspaceCommandUseCase.acceptInvite(token, createdUser);
        }

        // 로그인
        UserRequestDTO.LoginRequest loginRequest = UserRequestDTO.LoginRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        return userCommandUseCase.login(loginRequest);
    }

    @Override
    public UserResponseDTO.User signUp(UserRequestDTO.SignUpRequest request, String token) {

        // 유저 생성 및 저장
        User createdUser = userCommandUseCase.createUser(request);

        // 초대 토큰이 있는 경우, 초대 수락 로직 호출
        if (token != null)
            workspaceCommandUseCase.acceptInvite(token, createdUser);

        return UserConverter.toUserDTO(createdUser);
    }
}

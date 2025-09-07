package com.haru.api.user.application.service;

import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.application.port.out.AuthPort;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.application.port.in.UserCommandUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.EmailStatus;
import com.haru.api.workspace.application.port.in.WorkspaceCommandUseCase;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandUseCaseImpl implements UserCommandUseCase {

    private final UserPort userPort;
    private final AuthPort authPort;
    private final WorkspaceCommandUseCase workspaceCommandUseCase;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO.User signUp(UserRequestDTO.SignUpRequest request, String token) {

        // 유저 생성 및 저장
        User createdUser = createUser(request);

        // 초대 토큰이 있는 경우, 초대 수락 로직 호출
        if (token != null)
            workspaceCommandUseCase.acceptInvite(token, createdUser);

        return UserConverter.toUserDTO(createdUser);
    }

    @Override
    public UserResponseDTO.LoginResponse login(UserRequestDTO.LoginRequest request) {
        return authPort.login(request);
    }

    @Transactional
    @Override
    public UserResponseDTO.User updateUserInfo(User user, UserRequestDTO.UserInfoUpdateRequest request) {

        // 이름 수정 요청이 있을 경우
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.updateName(request.getName());
        }

        // 비밀번호 수정 요청이 있을 경우
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            // 새로운 비밀번호가 이전 비밀번호와 동일한지 확인
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new MemberHandler(ErrorStatus.SAME_WITH_OLD_PASSWORD);
            }

            // 새로운 비밀번호로 업데이트
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserConverter.toUserDTO(userPort.saveUser(user));
    }

    @Override
    public UserResponseDTO.RefreshResponse refresh(String refreshToken) {
        return authPort.refresh(refreshToken);
    }

    @Override
    public void logout(String accessToken) {
        authPort.logout(accessToken);
    }

    @Override
    public UserResponseDTO.CheckEmailDuplicationResponse checkEmailDuplication(UserRequestDTO.CheckEmailDuplicationRequest request) {

        User user = userPort.findUserByEmail(request.getEmail())
                .orElse(null);

        if (user == null) { // 해당 이메일을 사용하고 있는 유저가 존재하지 않을 경우
            return UserResponseDTO.CheckEmailDuplicationResponse.builder()
                    .emailStatus(EmailStatus.AVAILABLE)
                    .build();
        } else {
            return UserResponseDTO.CheckEmailDuplicationResponse.builder()
                    .emailStatus(EmailStatus.UNAVAILABLE)
                    .build();
        }
    }

    @Override
    public UserResponseDTO.CheckOriginalPasswordResponse checkOriginalPassword(UserRequestDTO.CheckOriginalPasswordRequest request, User user) {
        return UserConverter.toCheckOriginalPassword(passwordEncoder.matches(request.getRequestPassword(), user.getPassword()));
    }


    @Override
    public User createUser(UserRequestDTO.SignUpRequest request) {

        // 이메일 중복 확인
        if (userPort.existsUserByEmail(request.getEmail())) {
            throw new MemberHandler(ErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        String password = passwordEncoder.encode(request.getPassword());

        User user = UserConverter.toUsers(request, password);

        return userPort.saveUser(user);
    }
}

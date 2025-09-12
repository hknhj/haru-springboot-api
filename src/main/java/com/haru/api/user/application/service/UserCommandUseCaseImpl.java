package com.haru.api.user.application.service;

import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.application.port.in.UserCommandUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.EmailStatus;
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

    private final PasswordEncoder passwordEncoder;

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

        return UserConverter.toUserDTO(userPort.save(user));
    }

    @Override
    public UserResponseDTO.CheckEmailDuplicationResponse checkEmailDuplication(UserRequestDTO.CheckEmailDuplicationRequest request) {

        boolean isDuplicate = userPort.existsByEmail(request.getEmail());

        if (!isDuplicate) { // 해당 이메일을 사용하고 있는 유저가 존재하지 않을 경우
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
        if (userPort.existsByEmail(request.getEmail())) {
            throw new MemberHandler(ErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        String password = passwordEncoder.encode(request.getPassword());

        User user = UserConverter.toUsers(request, password);

        return userPort.save(user);
    }
}

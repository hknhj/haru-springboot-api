package com.haru.api.user.application.service;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.application.port.in.UserQueryUseCase;
import com.haru.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryUseCaseImpl implements UserQueryUseCase {

    private final UserPort userPort;

    @Override
    public UserResponseDTO.User getUserInfo(User user) {

        return UserConverter.toUserDTO(user);
    }

    @Override
    public List<UserResponseDTO.User> getSimilarEmailUsers(User user, String email) {

        List<User> users = userPort.searchSimilarEmailUsers(email);

        return users.parallelStream()
                .map(eachUser -> UserResponseDTO.User.builder()
                        .id(eachUser.getId())
                        .email(eachUser.getEmail())
                        .imageUrl(eachUser.getProfileImage())
                        .name(eachUser.getName())
                        .build())
                .toList();
    }

    @Override
    public User findUserById(Long userId) {
        return userPort.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Optional<User> findOptionalUserByEmail(String email) {
        return userPort.findByEmail(email);
    }
}

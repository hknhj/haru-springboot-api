package com.haru.api.user.application.service;

import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.application.port.in.UserQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryUseCaseImpl implements UserQueryUseCase {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO.User getUserInfo(User user) {

        return UserConverter.toUserDTO(user);
    }

    @Override
    public List<UserResponseDTO.User> getSimilarEmailUsers(User user, String email) {

        List<User> users = userRepository.findTop4UsersByEmailContainingIgnoreCase(email);

        return users.parallelStream()
                .map(eachUser -> UserResponseDTO.User.builder()
                        .id(eachUser.getId())
                        .email(eachUser.getEmail())
                        .imageUrl(eachUser.getProfileImage())
                        .name(eachUser.getName())
                        .build())
                .toList();
    }
}

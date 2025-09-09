package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryUseCase {

    UserResponseDTO.User getUserInfo(User user);

    List<UserResponseDTO.User> getSimilarEmailUsers(User user, String email);

    User findUserById(Long userId);

    Optional<User> findOptionalUserByEmail(String email);
}

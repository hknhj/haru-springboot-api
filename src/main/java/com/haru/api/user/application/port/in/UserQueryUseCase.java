package com.haru.api.user.application.port.in;

import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;

import java.util.List;

public interface UserQueryUseCase {

    UserResponseDTO.User getUserInfo(User user);

    List<UserResponseDTO.User> getSimilarEmailUsers(User user, String email);

    User findUserById(Long userId);
}

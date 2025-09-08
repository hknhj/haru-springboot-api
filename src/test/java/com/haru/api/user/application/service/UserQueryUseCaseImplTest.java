package com.haru.api.user.application.service;

import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserQueryUseCaseImplTest {

    @InjectMocks
    private UserQueryUseCaseImpl userQueryUseCase;

    @Test
    @DisplayName("유저 정보 조회 성공")
    void getUserInfo() {

        // given
        User fakeUser = User.builder()
                .id(1L)
                .email("test@nate.com")
                .profileImage("aws.com")
                .name("testName")
                .build();

        // when
        UserResponseDTO.User response = userQueryUseCase.getUserInfo(fakeUser);

        // then
        assertThat(response.getId()).isEqualTo(fakeUser.getId());
        assertThat(response.getEmail()).isEqualTo(fakeUser.getEmail());
        assertThat(response.getName()).isEqualTo(fakeUser.getName());
        assertThat(response.getImageUrl()).isEqualTo(fakeUser.getProfileImage());

    }
}
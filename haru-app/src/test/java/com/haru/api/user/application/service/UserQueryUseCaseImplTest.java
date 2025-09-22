package com.haru.api.user.application.service;

import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserQueryUseCaseImplTest {

    @InjectMocks
    private UserQueryUseCaseImpl userQueryUseCase;

    @Mock
    private UserPort userPort;

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

    @Test
    @DisplayName("유사 이메일 사용자 검색 성공")
    void getSimilarEmailUsers_success() {

        // given
        String emailKeyword = "test";
        User searchingUser = User.builder().id(0L).build(); // 이 메서드에서는 사용되지 않으므로 간단히 생성

        User user1 = User.builder().id(1L).email("test1@example.com").name("Tester One").profileImage("url1").build();
        User user2 = User.builder().id(2L).email("test2@example.com").name("Tester Two").profileImage("url2").build();
        List<User> fakeUserList = List.of(user1, user2);

        given(userPort.searchSimilarEmailUsers(emailKeyword)).willReturn(fakeUserList);

        // when
        List<UserResponseDTO.User> response = userQueryUseCase.getSimilarEmailUsers(searchingUser, emailKeyword);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getId()).isEqualTo(user1.getId());
        assertThat(response.get(0).getEmail()).isEqualTo(user1.getEmail());
        assertThat(response.get(1).getName()).isEqualTo(user2.getName());
        assertThat(response.get(1).getImageUrl()).isEqualTo(user2.getProfileImage());

        verify(userPort).searchSimilarEmailUsers(emailKeyword);
    }

    @Test
    @DisplayName("유사 이메일 사용자 검색 성공 - 결과 없음")
    void getSimilarEmailUsers_returns_empty_list_when_no_users_found() {

        // given
        String emailKeyword = "notfound";
        User searchingUser = User.builder().id(0L).build();

        given(userPort.searchSimilarEmailUsers(emailKeyword)).willReturn(Collections.emptyList());

        // when
        List<UserResponseDTO.User> response = userQueryUseCase.getSimilarEmailUsers(searchingUser, emailKeyword);

        // then
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        verify(userPort).searchSimilarEmailUsers(emailKeyword);
    }

}
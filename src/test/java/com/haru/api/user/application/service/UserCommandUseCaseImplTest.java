package com.haru.api.user.application.service;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.user.application.port.out.AuthPort;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.EmailStatus;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserCommandUseCaseImplTest {

    @InjectMocks
    private UserCommandUseCaseImpl userCommandUseCase;

    @Mock
    private UserPort userPort;

    @Mock
    private AuthPort authPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("유저 생성 및 저장 성공")
    void createUser() {

        // given
        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email("test@nate.com")
                .password("test1234")
                .name("testNickname")
                .marketingAgreed(true)
                .build();

        given(userPort.existsUserByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userPort.saveUser(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User createdUser = userCommandUseCase.createUser(request);

        // then
        assertThat(createdUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(createdUser.getPassword()).isEqualTo("encodedPassword");

        verify(userPort).saveUser(any(User.class));
    }

    @Test
    @DisplayName("유저 생성 및 저장 실패 - 중복된 이메일")
    void createUser_fail_when_duplicate_email() {

        // given
        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email("test@nate.com")
                .password("test1234")
                .name("testNickname")
                .marketingAgreed(true)
                .build();

        given(userPort.existsUserByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCommandUseCase.createUser(request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining(ErrorStatus.MEMBER_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("이메일 중복 검사 - 중복X")
    void check_email_duplication_with_no_duplicate_email() {

        // given
        UserRequestDTO.CheckEmailDuplicationRequest request = UserRequestDTO.CheckEmailDuplicationRequest.builder()
                .email("test@nate.com")
                .build();
        UserResponseDTO.CheckEmailDuplicationResponse expectedResponse = UserResponseDTO.CheckEmailDuplicationResponse.builder()
                .emailStatus(EmailStatus.AVAILABLE)
                .build();

        given(userPort.existsUserByEmail(request.getEmail())).willReturn(false);

        // when
        UserResponseDTO.CheckEmailDuplicationResponse actualResponse = userCommandUseCase.checkEmailDuplication(request);

        // then
        assertThat(actualResponse.getEmailStatus()).isEqualTo(expectedResponse.getEmailStatus());

        verify(userPort, times(1)).existsUserByEmail(request.getEmail());
    }

    @Test
    @DisplayName("이메일 중복 검사 - 중복O")
    void check_email_duplication_with_duplicate_email() {

        // given
        UserRequestDTO.CheckEmailDuplicationRequest request = UserRequestDTO.CheckEmailDuplicationRequest.builder()
                .email("test@nate.com")
                .build();
        UserResponseDTO.CheckEmailDuplicationResponse expectedResponse = UserResponseDTO.CheckEmailDuplicationResponse.builder()
                .emailStatus(EmailStatus.UNAVAILABLE)
                .build();

        given(userPort.existsUserByEmail(request.getEmail())).willReturn(true);

        // when
        UserResponseDTO.CheckEmailDuplicationResponse actualResponse = userCommandUseCase.checkEmailDuplication(request);

        // then
        assertThat(actualResponse.getEmailStatus()).isEqualTo(expectedResponse.getEmailStatus());

        verify(userPort, times(1)).existsUserByEmail(request.getEmail());
    }

    @Test
    @DisplayName("기존 비밀번호 일치 - 일치 O")
    void check_original_password_with_right_password() {

        // given
        String plainPassword = "originalPassword";
        String encodedPassword = "encodedPassword";

        UserRequestDTO.CheckOriginalPasswordRequest request = UserRequestDTO.CheckOriginalPasswordRequest.builder()
                .requestPassword(plainPassword)
                .build();
        User savedUser = User.builder()
                .password(encodedPassword)
                .build();

        given(passwordEncoder.matches(plainPassword, encodedPassword)).willReturn(true);

        // when
        UserResponseDTO.CheckOriginalPasswordResponse response = userCommandUseCase.checkOriginalPassword(request, savedUser);

        // then
        assertThat(response.getIsMatched()).isTrue();
    }

    @Test
    @DisplayName("기존 비밀번호 일치 - 일치 X")
    void check_original_password_with_wrong_password() {

        // given
        String wrongPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        UserRequestDTO.CheckOriginalPasswordRequest request = UserRequestDTO.CheckOriginalPasswordRequest.builder()
                .requestPassword(wrongPassword)
                .build();
        User savedUser = User.builder()
                .password(encodedPassword)
                .build();

        given(passwordEncoder.matches(wrongPassword, encodedPassword)).willReturn(false);

        // when
        UserResponseDTO.CheckOriginalPasswordResponse response = userCommandUseCase.checkOriginalPassword(request, savedUser);

        // then
        assertThat(response.getIsMatched()).isFalse();
    }

    @Test
    @DisplayName("유저 정보 업데이트 성공")
    void update_user_info() {

        String originalName = "originalName";
        String modifiedName = "modifiedName";

        String originalPassword = "originalPassword";
        String modifiedPassword = "modifiedPassword";
        String encodedPassword = "encodedPassword";

        // given
        UserRequestDTO.UserInfoUpdateRequest request = UserRequestDTO.UserInfoUpdateRequest.builder()
                .name(modifiedName)
                .password(modifiedPassword)
                .build();

        User user = spy(User.builder()
                .id(1L)
                .name(originalName)
                .password(originalPassword)
                .build());

        given(passwordEncoder.matches(modifiedPassword, originalPassword)).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);
        given(userPort.saveUser(user)).willReturn(user);

        // when
        UserResponseDTO.User response = userCommandUseCase.updateUserInfo(user, request);

        // then
        assertThat(response.getName()).isEqualTo(modifiedName);

        // spy 객체에서 update 메서드가 실행되었는지 확인 (mock 객체였으면 확인 불가능)
        verify(user, times(1)).updateName(modifiedName);
        verify(user, times(1)).updatePassword(encodedPassword);

        verify(passwordEncoder, times(1)).encode(modifiedPassword);
        verify(userPort, times(1)).saveUser(user);
    }

    @Test
    @DisplayName("유저 정보 업데이트 실패 - 기존 비밀번호와 변경하고자 하는 비밀번호 일치")
    void update_user_info_fail() {

        String originalName = "originalName";
        String modifiedName = "modifiedName";

        String originalPassword = "originalPassword";

        // given
        UserRequestDTO.UserInfoUpdateRequest request = UserRequestDTO.UserInfoUpdateRequest.builder()
                .name(modifiedName)
                .password(originalPassword)
                .build();

        User originalUser = User.builder()
                .name(originalName)
                .password(originalPassword)
                .build();

        given(passwordEncoder.matches(request.getPassword(), originalUser.getPassword())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCommandUseCase.updateUserInfo(originalUser, request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining(ErrorStatus.SAME_WITH_OLD_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("로그인 성공")
    void login() {

        // given
        UserRequestDTO.LoginRequest request = UserRequestDTO.LoginRequest.builder()
                .email("test@nate.com")
                .password("testPassword")
                .build();
        UserResponseDTO.LoginResponse expectedResponse = UserResponseDTO.LoginResponse.builder()
                .userId(1L)
                .accessToken("testAccessToken")
                .refreshToken("testRefreshToken")
                .build();

        given(authPort.login(request)).willReturn(expectedResponse);

        // when
        UserResponseDTO.LoginResponse actualResponse = userCommandUseCase.login(request);

        // then
        assertThat(expectedResponse).isEqualTo(actualResponse);

        verify(authPort, times(1)).login(request);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout() {

        // given
        String accessToken = "logoutAccessToken";

        // when
        authPort.logout(accessToken);

        // then
        verify(authPort, times(1)).logout(accessToken);
    }
}
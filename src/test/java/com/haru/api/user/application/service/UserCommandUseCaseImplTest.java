package com.haru.api.user.application.service;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserCommandUseCaseImplTest {

    @InjectMocks
    private UserCommandUseCaseImpl userCommandUseCase;

    @Mock
    private UserPort userPort;

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
}
package com.haru.api.user.infrastructure.adapter;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.infra.security.jwt.JwtUtils;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthImplTest {

    @InjectMocks
    private AuthImpl authPort;

    @Mock
    private UserPort userPort;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("로그인 성공")
    void login() {

        // given
        String testEmail = "test@nate.com";
        String fakeAccessToken = "fakeAccessToken";
        String fakeRefreshToken = "fakeRefreshToken";

        UserRequestDTO.LoginRequest request = UserRequestDTO.LoginRequest.builder()
                .email(testEmail)
                .password("testPassword")
                .build();

        User fakeUser = User.builder()
                .id(1L)
                .email(testEmail)
                .name("testName")
                .build();

        // spring security 인증 흐름 mocking
        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(authentication.getName()).willReturn(fakeUser.getEmail());

        given(userPort.findUserByEmail(testEmail)).willReturn(Optional.of(fakeUser));
        given(jwtUtils.generateAccessToken(fakeUser.getId())).willReturn(fakeAccessToken);
        given(jwtUtils.generateAndSaveRefreshToken(any(String.class))).willReturn(fakeRefreshToken);

        // when
        UserResponseDTO.LoginResponse response = authPort.login(request);

        // then
        assertThat(response.getUserId()).isEqualTo(fakeUser.getId());
        assertThat(response.getAccessToken()).isEqualTo(fakeAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(fakeRefreshToken);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userPort).findUserByEmail(fakeUser.getEmail());
        verify(jwtUtils).generateAccessToken(fakeUser.getId());

    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_with_not_existing_email() {

        // given
        UserRequestDTO.LoginRequest request = UserRequestDTO.LoginRequest.builder()
                .email("test@nate.com")
                .password("testPassword")
                .build();

        // authenticate() 메서드가 예외를 던지도록 설정
        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new MemberHandler(ErrorStatus.MEMBER_USERNAME_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> authPort.login(request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("해당 아이디를 가진 유저가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 일치하지 않는 비밀번호")
    void login_fail_with_not_matching_password() {

        // given
        UserRequestDTO.LoginRequest request = UserRequestDTO.LoginRequest.builder()
                .email("test@nate.com")
                .password("testPassword")
                .build();

        // authenticate() 메서드가 예외를 던지도록 설정
        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new MemberHandler(ErrorStatus.MEMBER_PASSWORD_NOT_MATCH));

        // when & then
        assertThatThrownBy(() -> authPort.login(request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }
}
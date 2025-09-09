package com.haru.api.user.infrastructure.adapter;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.infra.security.jwt.JwtUtils;
import com.haru.api.infra.security.jwt.SecurityUtil;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MockedStatic<SecurityUtil> mockSecurityUtil;

    @BeforeEach
    void setUp() {
        mockSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // 매 테스트 후 정적 메서드 Mocking 해제
        mockSecurityUtil.close();
    }

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

        given(userPort.findByEmail(testEmail)).willReturn(Optional.of(fakeUser));
        given(jwtUtils.generateAccessToken(fakeUser.getId())).willReturn(fakeAccessToken);
        given(jwtUtils.generateAndSaveRefreshToken(any(String.class))).willReturn(fakeRefreshToken);

        // when
        UserResponseDTO.LoginResponse response = authPort.login(request);

        // then
        assertThat(response.getUserId()).isEqualTo(fakeUser.getId());
        assertThat(response.getAccessToken()).isEqualTo(fakeAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(fakeRefreshToken);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userPort).findByEmail(fakeUser.getEmail());
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

    @Test
    @DisplayName("로그아웃 성공")
    void logout() {

        // given
        String accessToken = "fakeAccessToken";
        Long fakeUserId = 1L;
        long tokenRemainTime = 1800L;

        mockSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(fakeUserId);

        given(redisTemplate.delete(any(String.class))).willReturn(true);
        given(jwtUtils.tokenRemainTimeSecond(accessToken)).willReturn(tokenRemainTime);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        authPort.logout(accessToken);

        // then
        mockSecurityUtil.verify(SecurityUtil::getCurrentUserId);
        verify(redisTemplate).delete("users:" + fakeUserId);
        verify(jwtUtils).tokenRemainTimeSecond(accessToken);
        verify(valueOperations).set(
                "blackList:" + fakeUserId,
                accessToken,
                tokenRemainTime,
                TimeUnit.SECONDS
        );
    }

    @Test
    @DisplayName("access token 갱신 성공")
    void refresh_token() {

        // given
        String refreshToken = "fakeRefreshToken";
        String accessToken = "fakeAccessToken";
        String newRefreshToken = "fakeNewRefreshToken";
        Long fakeUserId = 1L;

        mockSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(fakeUserId);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any(String.class))).willReturn(refreshToken);
        given(jwtUtils.generateAccessToken(any(Long.class))).willReturn(accessToken);
        given(jwtUtils.generateAndSaveRefreshToken(any(String.class))).willReturn(newRefreshToken);

        // when
        UserResponseDTO.RefreshResponse response = authPort.refresh(refreshToken);

        // then
        assertThat(response.getUserId()).isEqualTo(fakeUserId);
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);

        verify(valueOperations).get("users:" + fakeUserId);
        verify(jwtUtils).generateAccessToken(fakeUserId);
        verify(jwtUtils).generateAndSaveRefreshToken("users:" + fakeUserId);
    }

    @Test
    @DisplayName("access token 갱신 실패 - 유효하지 않은 refresh token")
    void refresh_token_fail_with_invalid_refresh_token() {

        // given
        String refreshToken = "fakeRefreshToken";
        String wrongRefreshToken = "fakeWrongRefreshToken";
        Long fakeUserId = 1L;

        mockSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(fakeUserId);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(any(String.class))).willReturn(wrongRefreshToken);

        // when & then
        assertThatThrownBy(() -> authPort.refresh(refreshToken))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("리프레시 토큰이 일치하지 않습니다.");

        verify(jwtUtils, never()).generateAccessToken(anyLong());
        verify(jwtUtils, never()).generateAndSaveRefreshToken(anyString());
    }
}
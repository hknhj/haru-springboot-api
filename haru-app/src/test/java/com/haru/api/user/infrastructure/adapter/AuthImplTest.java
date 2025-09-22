package com.haru.api.user.infrastructure.adapter;

import com.haru.api.auth.application.port.out.AuthUserPort;
import com.haru.api.auth.application.port.out.AuthenticatePort;
import com.haru.api.auth.application.port.out.TokenPort;
import com.haru.api.auth.application.service.LoginUseCaseImpl;
import com.haru.api.auth.application.service.LogoutUseCaseImpl;
import com.haru.api.auth.application.service.TokenUseCaseImpl;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.infra.security.jwt.JwtUtils;
import com.haru.api.infra.security.jwt.SecurityUtil;
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
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthImplTest {

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;
    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;
    @InjectMocks
    private TokenUseCaseImpl tokenUseCase;

    @Mock
    private Authentication authentication;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private TokenPort tokenPort;
    @Mock
    private AuthenticatePort authenticatePort;
    @Mock
    private AuthUserPort authUserPort;

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

        given(authenticatePort.authenticate(request.getEmail(), request.getPassword())).willReturn(authentication);
        given(authUserPort.findByEmail(testEmail)).willReturn(Optional.of(fakeUser));
        given(authentication.getName()).willReturn(testEmail);
        given(tokenPort.generateAccessToken(fakeUser.getId())).willReturn(fakeAccessToken);
        given(tokenPort.generateAndSaveRefreshToken(fakeUser.getId())).willReturn(fakeRefreshToken);


        // when
        UserResponseDTO.LoginResponse response = loginUseCase.login(request);

        // then
        assertThat(response.getUserId()).isEqualTo(fakeUser.getId());
        assertThat(response.getAccessToken()).isEqualTo(fakeAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(fakeRefreshToken);

        verify(authenticatePort).authenticate(request.getEmail(), request.getPassword());
        verify(authUserPort).findByEmail(testEmail);
        verify(tokenPort).generateAccessToken(fakeUser.getId());
        verify(tokenPort).generateAndSaveRefreshToken(fakeUser.getId());

    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_with_not_existing_email() {

        // given
        UserRequestDTO.LoginRequest request = UserRequestDTO.LoginRequest.builder()
                .email("test@nate.com")
                .password("testPassword")
                .build();

        given(authenticatePort.authenticate(request.getEmail(), request.getPassword())).willReturn(authentication);
        given(authentication.getName()).willReturn("test@nate.com");
        given(authUserPort.findByEmail(any(String.class)))
                .willThrow(new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("사용자가 없습니다.");
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
        given(tokenPort.tokenRemainTimeSecond(accessToken)).willReturn(tokenRemainTime);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        logoutUseCase.logout(accessToken);

        // then
        mockSecurityUtil.verify(SecurityUtil::getCurrentUserId);
        verify(redisTemplate).delete("users:" + fakeUserId);
        verify(tokenPort).tokenRemainTimeSecond(accessToken);
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
        given(tokenPort.generateAccessToken(any(Long.class))).willReturn(accessToken);
        given(tokenPort.generateAndSaveRefreshToken(any(Long.class))).willReturn(newRefreshToken);

        // when
        UserResponseDTO.RefreshResponse response = tokenUseCase.refresh(refreshToken);

        // then
        assertThat(response.getUserId()).isEqualTo(fakeUserId);
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);

        verify(valueOperations).get("users:" + fakeUserId);
        verify(tokenPort).generateAccessToken(fakeUserId);
        verify(tokenPort).generateAndSaveRefreshToken(fakeUserId);
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
        assertThatThrownBy(() -> tokenUseCase.refresh(refreshToken))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("리프레시 토큰이 일치하지 않습니다.");

        verify(jwtUtils, never()).generateAccessToken(anyLong());
        verify(jwtUtils, never()).generateAndSaveRefreshToken(anyString());
    }
}
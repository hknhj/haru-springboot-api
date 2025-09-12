package com.haru.api.user.application.service;

import com.haru.api.auth.application.facade.AuthFacade;
import com.haru.api.user.application.port.in.UserCommandUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.workspace.application.port.in.WorkspaceCommandUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserSignUpWorkflowUseCaseImplTest {

    @InjectMocks
    private UserSignUpWorkflowUseCaseImpl userSignUpWorkflowUseCase;

    @Mock
    private UserCommandUseCase userCommandUseCase;
    @Mock
    private WorkspaceCommandUseCase workspaceCommandUseCase;
    @Mock
    private AuthFacade authFacade;

    @Test
    @DisplayName("회원가입 성공 - 토큰 X")
    void sign_up_without_token() {

        // given
        String email = "test@nate.com";
        String name = "testName";

        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email(email)
                .name(name)
                .build();

        User fakeUser = User.builder()
                .email(email)
                .name(name)
                .build();

        given(userCommandUseCase.createUser(request)).willReturn(fakeUser);

        // when
        UserResponseDTO.User response = userSignUpWorkflowUseCase.signUp(request, null);

        // then
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);

        verify(userCommandUseCase, times(1)).createUser(request);
        verify(workspaceCommandUseCase, never()).acceptInvite(any(String.class), any(User.class));
    }

    @Test
    @DisplayName("회원가입 성공 - 토큰 O")
    void sign_up_with_token() {

        // given
        String email = "test@nate.com";
        String name = "testName";
        String token = "fakeInvitationToken";

        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email(email)
                .name(name)
                .build();

        User fakeUser = User.builder()
                .email(email)
                .name(name)
                .build();

        given(userCommandUseCase.createUser(request)).willReturn(fakeUser);

        // when
        UserResponseDTO.User response = userSignUpWorkflowUseCase.signUp(request, token);

        // then
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
        verify(userCommandUseCase, times(1)).createUser(request);
        verify(workspaceCommandUseCase, times(1)).acceptInvite(token, fakeUser);
    }

    @Test
    @DisplayName("회원가입 후 로그인 성공 - 토큰 X")
    void sign_up_and_login_without_token() {

        // given
        String testEmail = "test@nate.com";
        String testPassword = "testPassword";
        String testEncodedPassword = "testEncodedPassword";
        String testName = "testName";

        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .name(testName)
                .build();

        User fakeUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password(testEncodedPassword)
                .name(request.getName())
                .build();

        UserResponseDTO.LoginResponse fakeResponse = UserResponseDTO.LoginResponse.builder()
                .userId(1L)
                .accessToken("fakeAccessToken")
                .refreshToken("fakeRefreshToken")
                .build();

        given(userCommandUseCase.createUser(request)).willReturn(fakeUser);
        given(authFacade.login(any(UserRequestDTO.LoginRequest.class))).willReturn(fakeResponse);

        // when
        UserResponseDTO.LoginResponse response = userSignUpWorkflowUseCase.signUpAndLogin(request, null);

        // then
        assertThat(response).isEqualTo(fakeResponse);

        // createUser가 먼저 호출되고, login이 나중에 호출되었는지 순서 검증
        InOrder inOrder = inOrder(userCommandUseCase);
        inOrder.verify(userCommandUseCase).createUser(request);
        inOrder.verify(authFacade).login(any(UserRequestDTO.LoginRequest.class));

        // acceptInvite는 절대 호출되지 않았는지 검증
        verify(workspaceCommandUseCase, never()).acceptInvite(any(), any());

    }

    @Test
    @DisplayName("회원가입 후 로그인 성공 - 토큰 O")
    void sign_up_and_login_with_token() {

        // given
        String testEmail = "test@nate.com";
        String testPassword = "testPassword";
        String testEncodedPassword = "testEncodedPassword";
        String testName = "testName";
        String token = "fakeInvitationToken";

        UserRequestDTO.SignUpRequest request = UserRequestDTO.SignUpRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .name(testName)
                .build();

        User fakeUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password(testEncodedPassword)
                .name(request.getName())
                .build();

        UserResponseDTO.LoginResponse fakeResponse = UserResponseDTO.LoginResponse.builder()
                .userId(1L)
                .accessToken("fakeAccessToken")
                .refreshToken("fakeRefreshToken")
                .build();

        given(userCommandUseCase.createUser(request)).willReturn(fakeUser);
        given(authFacade.login(any(UserRequestDTO.LoginRequest.class))).willReturn(fakeResponse);

        // when
        UserResponseDTO.LoginResponse response = userSignUpWorkflowUseCase.signUpAndLogin(request, token);

        // then
        assertThat(response).isEqualTo(fakeResponse);

        // createUser가 먼저 호출되고, login이 나중에 호출되었는지 순서 검증
        InOrder inOrder = inOrder(userCommandUseCase, workspaceCommandUseCase);
        inOrder.verify(userCommandUseCase).createUser(request);
        inOrder.verify(workspaceCommandUseCase).acceptInvite(eq(token), eq(fakeUser));
        inOrder.verify(authFacade).login(any(UserRequestDTO.LoginRequest.class));

        // acceptInvite가 호출되었는지 검증
        verify(workspaceCommandUseCase, times(1)).acceptInvite(token, fakeUser);
    }
}
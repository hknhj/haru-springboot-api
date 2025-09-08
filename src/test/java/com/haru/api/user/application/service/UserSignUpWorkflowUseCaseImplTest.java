package com.haru.api.user.application.service;

import com.haru.api.user.application.port.in.UserCommandUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.presentation.dto.UserRequestDTO;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.workspace.application.port.in.WorkspaceCommandUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
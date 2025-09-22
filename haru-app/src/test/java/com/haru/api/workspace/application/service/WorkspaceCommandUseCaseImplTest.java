package com.haru.api.workspace.application.service;

import com.haru.api.global.apiPayload.exception.handler.UserWorkspaceHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceInvitationHandler;
import com.haru.api.infra.mail.EmailSender;
import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.common_library.application.port.in.DocumentQueryUseCase;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.user.application.port.in.UserQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.application.port.out.WorkspaceInvitationPort;
import com.haru.api.workspace.application.port.out.WorkspacePort;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.domain.WorkspaceInvitation;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.presentation.dto.WorkspaceRequestDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceCommandUseCaseImplTest {

    @InjectMocks
    private WorkspaceCommandUseCaseImpl workspaceCommandUseCase;

    @Mock
    private AmazonS3Manager amazonS3Manager;
    @Mock
    private WorkspacePort workspacePort;
    @Mock
    private UserWorkspacePort userWorkspacePort;
    @Mock
    private WorkspaceInvitationPort workspaceInvitationPort;
    @Mock
    private UserDocumentLastOpenedCommandUseCase userDocumentLastOpenedCommandUseCase;
    @Mock
    private UserQueryUseCase userQueryUseCase;
    @Mock
    private DocumentQueryUseCase documentQueryUseCase;
    @Mock
    private EmailSender emailSender;

    private final String inviteBaseUrl = "https://my-app.com/invite";

    @Test
    @DisplayName("워크스페이스 생성 성공 - 이미지 포함")
    void createWorkspace_success_with_image() {

        // given
        User user = User.builder().id(1L).build();
        WorkspaceRequestDTO.WorkspaceCreateRequest request = new WorkspaceRequestDTO.WorkspaceCreateRequest("My Workspace");
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

        String fakeKeyName = "workspace/image/generated-key.jpg";
        String fakePresignedUrl = "https://s3.presigned.url/workspace/image/generated-key.jpg";

        given(amazonS3Manager.generateKeyName(anyString())).willReturn(fakeKeyName);
        given(amazonS3Manager.uploadMultipartFile(fakeKeyName, image)).willReturn(fakeKeyName);
        given(workspacePort.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userWorkspacePort.save(any(UserWorkspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(amazonS3Manager.generatePresignedUrl(fakeKeyName)).willReturn(fakePresignedUrl);

        // when
        WorkspaceResponseDTO.Workspace response = workspaceCommandUseCase.createWorkspace(user, request, image);

        // then
        // 상태 검증
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getImageUrl()).isEqualTo(fakePresignedUrl);

        // 행위 검증
        // ArgumentCaptor를 사용하여 workspacePort.save에 전달된 Workspace 객체를 캡처
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspacePort).save(workspaceCaptor.capture());
        assertThat(workspaceCaptor.getValue().getKeyName()).isEqualTo(fakeKeyName);

        verify(userWorkspacePort).save(any(UserWorkspace.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 성공 - 이미지 없음")
    void createWorkspace_success_without_image() {
        // given
        User user = User.builder().id(1L).build();
        WorkspaceRequestDTO.WorkspaceCreateRequest request = new WorkspaceRequestDTO.WorkspaceCreateRequest("My Workspace");
        MultipartFile nullImage = null;

        given(workspacePort.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userWorkspacePort.save(any(UserWorkspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(amazonS3Manager.generatePresignedUrl(null)).willReturn(null);

        // when
        WorkspaceResponseDTO.Workspace response = workspaceCommandUseCase.createWorkspace(user, request, nullImage);

        // then
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getImageUrl()).isNull();

        // S3 업로드 관련 메서드는 호출되지 않아야 함
        verify(amazonS3Manager, never()).generateKeyName(anyString());
        verify(amazonS3Manager, never()).uploadMultipartFile(anyString(), any(MultipartFile.class));

        // DB 저장 로직은 호출되어야 함
        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspacePort).save(workspaceCaptor.capture());
        assertThat(workspaceCaptor.getValue().getKeyName()).isNull();
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 - 멤버가 아님")
    void updateWorkspace_fail_when_not_member() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        WorkspaceRequestDTO.WorkspaceUpdateRequest request = new WorkspaceRequestDTO.WorkspaceUpdateRequest("new title");

        given(userWorkspacePort.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.updateWorkspace(user, workspace, request, null))
                .isInstanceOf(UserWorkspaceHandler.class)
                .hasMessageContaining("해당 유저가 해당 워크스페이스에 속해있지 않습니다.");
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 - 어드민이 아님")
    void updateWorkspace_fail_when_not_admin() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        WorkspaceRequestDTO.WorkspaceUpdateRequest request = new WorkspaceRequestDTO.WorkspaceUpdateRequest("new title");
        UserWorkspace member = UserWorkspace.builder().auth(Auth.MEMBER).build();

        given(userWorkspacePort.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.updateWorkspace(user, workspace, request, null))
                .isInstanceOf(WorkspaceHandler.class)
                .hasMessageContaining("워크스페이스 수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("워크스페이스 수정 성공 - 제목과 이미지 모두 변경 (기존 이미지 있음)")
    void updateWorkspace_success_with_title_and_image() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = spy(Workspace.builder().id(10L).keyName("existing-key.jpg").build());
        WorkspaceRequestDTO.WorkspaceUpdateRequest request = new WorkspaceRequestDTO.WorkspaceUpdateRequest("new title");
        MockMultipartFile newImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "new image".getBytes());
        UserWorkspace admin = UserWorkspace.builder().auth(Auth.ADMIN).build();

        given(userWorkspacePort.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())).willReturn(Optional.of(admin));
        given(amazonS3Manager.generatePresignedUrl(anyString())).willReturn("presigned-url");

        // when
        WorkspaceResponseDTO.Workspace response = workspaceCommandUseCase.updateWorkspace(user, workspace, request, newImage);

        // then
        assertThat(response.getTitle()).isEqualTo("new title");
        assertThat(response.getImageUrl()).isEqualTo("presigned-url");

        verify(workspace).updateTitle("new title");
        verify(amazonS3Manager, never()).generateKeyName(anyString());
        verify(amazonS3Manager).uploadMultipartFile("existing-key.jpg", newImage);
        verify(workspacePort).save(workspace);
    }

    @Test
    @DisplayName("워크스페이스 수정 성공 - 첫 이미지 등록")
    void updateWorkspace_success_with_first_image() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = spy(Workspace.builder().id(10L).keyName(null).build());
        WorkspaceRequestDTO.WorkspaceUpdateRequest request = new WorkspaceRequestDTO.WorkspaceUpdateRequest("new title");
        MockMultipartFile firstImage = new MockMultipartFile("image", "first.jpg", "image/jpeg", "first image".getBytes());
        UserWorkspace admin = UserWorkspace.builder().auth(Auth.ADMIN).build();

        String newKeyName = "workspace/image/new-key.jpg";
        given(userWorkspacePort.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())).willReturn(Optional.of(admin));
        given(amazonS3Manager.generateKeyName(anyString())).willReturn(newKeyName);

        // when
        workspaceCommandUseCase.updateWorkspace(user, workspace, request, firstImage);

        // then
        verify(workspace).initKeyName(newKeyName);
        verify(amazonS3Manager).generateKeyName("workspace/image");
        verify(amazonS3Manager).uploadMultipartFile(newKeyName, firstImage);
        verify(workspacePort).save(workspace);
    }

    @Test
    @DisplayName("초대 수락 실패 - 유효하지 않은 토큰")
    void acceptInvite_fail_when_token_not_found() {

        // given
        String invalidToken = "invalid-token";
        given(workspaceInvitationPort.findByToken(invalidToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.acceptInvite(invalidToken))
                .isInstanceOf(WorkspaceInvitationHandler.class)
                .hasMessageContaining("초대 코드에 해당하는 초대장이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("초대 수락 실패 - 이미 수락된 초대장")
    void acceptInvite_fail_when_already_accepted() {

        // given
        String token = "valid-token";
        WorkspaceInvitation acceptedInvitation = spy(WorkspaceInvitation.builder().build());

        given(workspaceInvitationPort.findByToken(token)).willReturn(Optional.of(acceptedInvitation));
        given(acceptedInvitation.isAccepted()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.acceptInvite(token))
                .isInstanceOf(WorkspaceInvitationHandler.class)
                .hasMessageContaining("이미 초대가 수락된 초대장입니다.");
    }

    @Test
    @DisplayName("초대 수락 - 미가입 사용자")
    void acceptInvite_when_user_not_registered() {

        // given
        String token = "valid-token";
        String inviteeEmail = "new@test.com";
        Workspace workspace = Workspace.builder().id(1L).build();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder().email(inviteeEmail).workspace(workspace).isAccepted(false).build();

        given(workspaceInvitationPort.findByToken(token)).willReturn(Optional.of(invitation));
        given(userQueryUseCase.findOptionalUserByEmail(inviteeEmail)).willReturn(Optional.empty());

        // when
        WorkspaceResponseDTO.InvitationAcceptResult response = workspaceCommandUseCase.acceptInvite(token);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isAlreadyRegistered()).isFalse();

        // 유저가 없으므로 초대장 상태 변경이나 저장은 일어나면 안 됨
        verify(workspaceInvitationPort, never()).save(any());
        verify(userWorkspacePort, never()).save(any());
    }

    @Test
    @DisplayName("초대 수락 성공 - 기가입 사용자")
    void acceptInvite_success_when_user_is_registered() {

        // given
        String token = "valid-token";
        String inviteeEmail = "existing@test.com";
        User existingUser = User.builder().id(100L).email(inviteeEmail).build();
        Workspace workspace = Workspace.builder().id(1L).build();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder().email(inviteeEmail).workspace(workspace).isAccepted(false).build();

        given(workspaceInvitationPort.findByToken(token)).willReturn(Optional.of(invitation));
        given(userQueryUseCase.findOptionalUserByEmail(inviteeEmail)).willReturn(Optional.of(existingUser));

        // when
        WorkspaceResponseDTO.InvitationAcceptResult response = workspaceCommandUseCase.acceptInvite(token);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.isAlreadyRegistered()).isTrue();
        assertThat(response.getWorkspaceId()).isEqualTo(workspace.getId());

        // 초대장의 isAccepted가 true로 변경되어 저장되었는지 검증
        // private 메서드 내부에서 호출하는 Port들이 올바르게 호출되었는지 확인
        verify(userWorkspacePort).save(any(UserWorkspace.class));
        verify(userDocumentLastOpenedCommandUseCase).saveAll(anyList());

        // 초대장 상태 변경 검증
        verify(workspaceInvitationPort).save(any(WorkspaceInvitation.class));
    }

    @Test
    @DisplayName("신규 유저 초대 수락 실패 - 유효하지 않은 토큰")
    void acceptInvite_forNewUser_fail_when_token_not_found() {

        // given
        String invalidToken = "invalid-token";
        User newUser = User.builder().id(1L).build();
        given(workspaceInvitationPort.findByToken(invalidToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.acceptInvite(invalidToken, newUser))
                .isInstanceOf(WorkspaceInvitationHandler.class)
                .hasMessageContaining("초대 코드에 해당하는 초대장이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("신규 유저 초대 수락 실패 - 이미 수락된 초대장")
    void acceptInvite_forNewUser_fail_when_already_accepted() {

        // given
        String token = "valid-token";
        User newUser = User.builder().id(1L).build();

        WorkspaceInvitation acceptedInvitation = mock(WorkspaceInvitation.class);
        given(acceptedInvitation.isAccepted()).willReturn(true);
        given(workspaceInvitationPort.findByToken(token)).willReturn(Optional.of(acceptedInvitation));

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.acceptInvite(token, newUser))
                .isInstanceOf(WorkspaceInvitationHandler.class)
                .hasMessageContaining("이미 초대가 수락된 초대장입니다.");
    }

    @Test
    @DisplayName("신규 유저 초대 수락 성공")
    void acceptInvite_forNewUser_success() {

        // given
        String token = "valid-token";
        User newUser = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspace)
                .isAccepted(false)
                .build();

        given(workspaceInvitationPort.findByToken(token)).willReturn(Optional.of(invitation));
        given(userWorkspacePort.save(any(UserWorkspace.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        workspaceCommandUseCase.acceptInvite(token, newUser);

        // then
        // 초대장의 상태가 '수락됨(accepted)'으로 변경되어 저장되었는지 검증
        ArgumentCaptor<WorkspaceInvitation> invitationCaptor = ArgumentCaptor.forClass(WorkspaceInvitation.class);
        verify(workspaceInvitationPort).save(invitationCaptor.capture());
        assertThat(invitationCaptor.getValue().isAccepted()).isTrue();

        // UserWorkspacePort.save가 호출되었는지 검증
        ArgumentCaptor<UserWorkspace> userWorkspaceCaptor = ArgumentCaptor.forClass(UserWorkspace.class);
        verify(userWorkspacePort).save(userWorkspaceCaptor.capture());

        // 저장된 UserWorkspace 객체의 user가 파라미터로 받은 newUser와 일치하는지 검증
        assertThat(userWorkspaceCaptor.getValue().getUser()).isEqualTo(newUser);
    }

    @Test
    @DisplayName("초대 이메일 발송 실패 - 워크스페이스 없음")
    void sendInviteEmail_fail_when_workspace_not_found() {

        // given
        User user = User.builder().id(1L).build();
        WorkspaceRequestDTO.WorkspaceInviteEmailRequest request = new WorkspaceRequestDTO.WorkspaceInviteEmailRequest(999L, List.of("test@test.com"));

        given(workspacePort.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.sendInviteEmail(user, request))
                .isInstanceOf(WorkspaceHandler.class)
                .hasMessageContaining("워크스페이스가 없습니다.");
    }

    @Test
    @DisplayName("초대 이메일 발송 실패 - 사용자가 멤버가 아님")
    void sendInviteEmail_fail_when_user_not_member() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        WorkspaceRequestDTO.WorkspaceInviteEmailRequest request = new WorkspaceRequestDTO.WorkspaceInviteEmailRequest(10L, List.of("test@test.com"));

        given(workspacePort.findById(10L)).willReturn(Optional.of(workspace));
        given(userWorkspacePort.findByUserIdAndWorkspaceId(1L, 10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceCommandUseCase.sendInviteEmail(user, request))
                .isInstanceOf(UserWorkspaceHandler.class)
                .hasMessageContaining("해당 유저가 해당 워크스페이스에 속해있지 않습니다.");
    }

    @Test
    @DisplayName("초대 이메일 발송 성공")
    void sendInviteEmail_success() {

        // given
        User invitingUser = User.builder().id(1L).name("초대자").build();
        Workspace workspace = Workspace.builder().id(10L).title("테스트 워크스페이스").build();
        List<String> emailsToInvite = List.of("invitee1@test.com", "invitee2@test.com");
        WorkspaceRequestDTO.WorkspaceInviteEmailRequest request = new WorkspaceRequestDTO.WorkspaceInviteEmailRequest(10L, emailsToInvite);

        UUID fakeUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fakeToken = fakeUuid.toString();

        given(workspacePort.findById(10L)).willReturn(Optional.of(workspace));
        given(userWorkspacePort.findByUserIdAndWorkspaceId(1L, 10L)).willReturn(Optional.of(mock(UserWorkspace.class)));

        // 정적 메서드 Mocking
        try (MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {
            mockedUuid.when(UUID::randomUUID).thenReturn(fakeUuid);

            // when
            workspaceCommandUseCase.sendInviteEmail(invitingUser, request);

            // then
            // 초대장 저장 로직 검증
            ArgumentCaptor<WorkspaceInvitation> invitationCaptor = ArgumentCaptor.forClass(WorkspaceInvitation.class);
            verify(workspaceInvitationPort, times(2)).save(invitationCaptor.capture());

            List<WorkspaceInvitation> savedInvitations = invitationCaptor.getAllValues();
            assertThat(savedInvitations.get(0).getEmail()).isEqualTo("invitee1@test.com");
            assertThat(savedInvitations.get(1).getEmail()).isEqualTo("invitee2@test.com");
            assertThat(savedInvitations.get(0).getToken()).isEqualTo(fakeToken);

            // 이메일 발송 로직 검증
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailSender, times(2)).send(emailCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());

            assertThat(emailCaptor.getAllValues()).containsExactly("invitee1@test.com", "invitee2@test.com");
            assertThat(subjectCaptor.getValue()).contains(workspace.getTitle(), invitingUser.getName());
        }
    }

}
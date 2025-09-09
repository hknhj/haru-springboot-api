package com.haru.api.workspace.application.service;

import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.application.port.out.WorkspacePort;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.presentation.dto.WorkspaceRequestDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
}
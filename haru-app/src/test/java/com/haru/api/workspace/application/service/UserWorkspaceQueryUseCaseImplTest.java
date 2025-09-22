package com.haru.api.workspace.application.service;

import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserWorkspaceQueryUseCaseImplTest {

    @InjectMocks
    private UserWorkspaceQueryUseCaseImpl userWorkspaceQueryUseCase;

    @Mock
    private UserWorkspacePort userWorkspacePort;

    @Mock
    private AmazonS3Manager amazonS3Manager;

    @Test
    @DisplayName("워크스페이스 목록 조회 성공")
    void getUserWorkspaceList_success() {

        // given
        User user = User.builder().id(1L).build();

        UserWorkspaceResponseDTO.UserWorkspaceWithTitle workspace1 = UserWorkspaceResponseDTO.UserWorkspaceWithTitle.builder()
                .workspaceId(10L)
                .title("ws1")
                .imageUrl("original-url-1")
                .build();
        UserWorkspaceResponseDTO.UserWorkspaceWithTitle workspace2 = UserWorkspaceResponseDTO.UserWorkspaceWithTitle.builder()
                .workspaceId(11L)
                .title("ws2")
                .imageUrl("original-url-2")
                .build();

        List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> fakeWorkspaceList = List.of(workspace1, workspace2);

        given(userWorkspacePort.getUserWorkspacesWithTitle(user.getId())).willReturn(fakeWorkspaceList);
        given(amazonS3Manager.generatePresignedUrl("original-url-1")).willReturn("presigned-url-1");
        given(amazonS3Manager.generatePresignedUrl("original-url-2")).willReturn("presigned-url-2");

        // when
        List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> response = userWorkspaceQueryUseCase.getUserWorkspaceList(user);

        // then
        // 상태 검증: 반환된 목록의 크기와 내용을 확인
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getImageUrl()).isEqualTo("presigned-url-1");
        assertThat(response.get(1).getImageUrl()).isEqualTo("presigned-url-2");

        // 행위 검증: S3 Manager가 각 URL에 대해 정확히 1번씩 호출되었는지 확인
        verify(amazonS3Manager, times(1)).generatePresignedUrl("original-url-1");
        verify(amazonS3Manager, times(1)).generatePresignedUrl("original-url-2");
    }

    @Test
    @DisplayName("워크스페이스 목록 조회 - 참여한 워크스페이스 없음")
    void getUserWorkspaceList_returns_empty_list_when_no_workspaces() {

        // given
        User user = User.builder().id(1L).build();

        given(userWorkspacePort.getUserWorkspacesWithTitle(user.getId())).willReturn(Collections.emptyList());

        // when
        List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> response = userWorkspaceQueryUseCase.getUserWorkspaceList(user);

        // then
        // 상태 검증: 반환된 리스트가 비어있는지 확인
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        // 행위 검증: S3 Manager가 한 번도 호출되지 않았는지 확인
        verify(amazonS3Manager, never()).generatePresignedUrl(anyString());
    }
}
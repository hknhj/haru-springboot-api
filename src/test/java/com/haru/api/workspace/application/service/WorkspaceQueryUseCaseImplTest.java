package com.haru.api.workspace.application.service;

import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkspaceQueryUseCaseImplTest {

    @InjectMocks
    private WorkspaceQueryUseCaseImpl workspaceQueryUseCase;

    @Mock
    private UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    @Test
    @DisplayName("문서 제목으로 검색 성공")
    void getDocumentsByTitle_success() {

        // given
        User user = User.builder()
                .id(1L)
                .build();

        Workspace workspace = Workspace.builder()
                .id(10L)
                .build();

        String titleKeyword = "test";

        UserDocumentLastOpened doc1 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 5L, DocumentType.AI_MEETING_MANAGER))
                .title("test document 1")
                .build();
        UserDocumentLastOpened doc2 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 7L, DocumentType.SNS_EVENT_ASSISTANT))
                .title("test document 2")
                .build();

        List<UserDocumentLastOpened> fakeDocumentList = List.of(doc1, doc2);

        given(userDocumentLastOpenedQueryUseCase.getRecentDocumentsByTitle(user.getId(), workspace.getId(), titleKeyword))
                .willReturn(fakeDocumentList);

        // when
        WorkspaceResponseDTO.DocumentList response = workspaceQueryUseCase.getDocumentsByTitle(user, workspace, titleKeyword);

        // then
        // 상태 검증: 반환된 DTO의 크기와 내용이 올바른지 확인
        assertThat(response.getDocuments()).hasSize(2);
        assertThat(response.getDocuments().get(0).getTitle()).isEqualTo(doc1.getTitle());

        // 행위 검증: 의존하는 UseCase가 올바른 인자들로 호출되었는지 확인
        verify(userDocumentLastOpenedQueryUseCase).getRecentDocumentsByTitle(user.getId(), workspace.getId(), titleKeyword);
    }

    @Test
    @DisplayName("문서 제목으로 검색 - 결과 없음")
    void getDocumentsByTitle_returns_empty_list_when_no_results() {
        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        String titleKeyword = "notfound";

        // Mock 객체가 비어 있는 리스트를 반환하도록 설정
        given(userDocumentLastOpenedQueryUseCase.getRecentDocumentsByTitle(user.getId(), workspace.getId(), titleKeyword))
                .willReturn(Collections.emptyList());

        // when
        WorkspaceResponseDTO.DocumentList response = workspaceQueryUseCase.getDocumentsByTitle(user, workspace, titleKeyword);

        // then
        assertThat(response.getDocuments()).isNotNull();
        assertThat(response.getDocuments()).isEmpty();

        verify(userDocumentLastOpenedQueryUseCase).getRecentDocumentsByTitle(user.getId(), workspace.getId(), titleKeyword);
    }

    @Test
    @DisplayName("사이드바 문서 목록 조회 성공")
    void getDocumentsForSidebar_success() {
        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();

        // 가짜 최근 문서 데이터 생성
        UserDocumentLastOpened doc1 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 5L, DocumentType.AI_MEETING_MANAGER))
                .title("test document 1")
                .build();
        UserDocumentLastOpened doc2 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 7L, DocumentType.SNS_EVENT_ASSISTANT))
                .title("test document 2")
                .build();
        List<UserDocumentLastOpened> fakeRecentDocuments = List.of(doc1, doc2);

        given(userDocumentLastOpenedQueryUseCase.getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class)))
                .willReturn(fakeRecentDocuments);

        // when
        WorkspaceResponseDTO.DocumentSidebarList response = workspaceQueryUseCase.getDocumentsForSidebar(user, workspace);

        // then
        // 행위 검증: 의존하는 UseCase가 올바른 인자들로 호출되었는지 확인
        verify(userDocumentLastOpenedQueryUseCase).getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class));

        // 상태 검증: 반환된 DTO의 크기와 내용이 올바른지 확인
        assertThat(response.getDocuments()).hasSize(2);
        assertThat(response.getDocuments().get(0).getTitle()).isEqualTo(doc1.getTitle());
    }

    @Test
    @DisplayName("사이드바 문서 목록 조회 - 결과 없음")
    void getDocumentsForSidebar_returns_empty_list_when_no_recent_documents() {
        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();

        // Mock 객체가 비어 있는 리스트를 반환하도록 설정
        given(userDocumentLastOpenedQueryUseCase.getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class)))
                .willReturn(Collections.emptyList());

        // when
        WorkspaceResponseDTO.DocumentSidebarList response = workspaceQueryUseCase.getDocumentsForSidebar(user, workspace);

        // then
        assertThat(response.getDocuments()).isNotNull();
        assertThat(response.getDocuments()).isEmpty();

        verify(userDocumentLastOpenedQueryUseCase).getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class));
    }
}
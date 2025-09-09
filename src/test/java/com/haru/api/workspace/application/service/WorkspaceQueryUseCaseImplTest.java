package com.haru.api.workspace.application.service;

import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.shared_kernel.application.port.in.DocumentQueryUseCase;
import com.haru.api.shared_kernel.domain.Documentable;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceQueryUseCaseImplTest {

    @InjectMocks
    private WorkspaceQueryUseCaseImpl workspaceQueryUseCase;

    @Mock
    private UserWorkspacePort userWorkspacePort;

    @Mock
    private UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    @Mock
    private DocumentQueryUseCase documentQueryUseCase;

    @Mock
    private AmazonS3Manager amazonS3Manager;

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

    @Test
    @DisplayName("캘린더 문서 목록 조회 성공")
    void getDocumentForCalendar_success() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        Documentable doc1 = Meeting.builder()
                .id(1L)
                .title("미팅1")
                .workspace(workspace)
                .build();
        Documentable doc2 = SnsEvent.builder()
                .id(2L)
                .title("이벤트1")
                .workspace(workspace)
                .build();
        List<Documentable> fakeDocumentList = List.of(doc1, doc2);

        given(documentQueryUseCase.getAllDocumentsForCalendars(eq(workspace.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(fakeDocumentList);

        // when
        WorkspaceResponseDTO.DocumentCalendarList response = workspaceQueryUseCase.getDocumentForCalendar(user, workspace, startDate, endDate);

        // then
        // 행위 검증: 의존하는 UseCase가 올바른 인자들로 호출되었는지 확인
        verify(documentQueryUseCase).getAllDocumentsForCalendars(eq(workspace.getId()), any(LocalDateTime.class), any(LocalDateTime.class));

        // 상태 검증: 반환된 DTO의 크기와 내용이 올바른지 확인
        assertThat(response.getDocumentList()).hasSize(2);
        assertThat(response.getDocumentList().get(0).getTitle()).isEqualTo(doc1.getTitle());
        assertThat(response.getDocumentList().get(1).getTitle()).isEqualTo(doc2.getTitle());
    }

    @Test
    @DisplayName("캘린더 문서 목록 조회 - 결과 없음")
    void getDocumentForCalendar_returns_empty_list_when_no_documents() {
        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        given(documentQueryUseCase.getAllDocumentsForCalendars(eq(workspace.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // when
        WorkspaceResponseDTO.DocumentCalendarList response = workspaceQueryUseCase.getDocumentForCalendar(user, workspace, startDate, endDate);

        // then
        verify(documentQueryUseCase).getAllDocumentsForCalendars(eq(workspace.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
        assertThat(response.getDocumentList()).isNotNull();
        assertThat(response.getDocumentList()).isEmpty();
    }

    @Test
    @DisplayName("워크스페이스 편집 페이지 조회 성공")
    void getEditPage_success() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder()
                .id(10L)
                .keyName("image-key.jpg")
                .build();

        User member1 = User.builder().id(2L).name("member1").build();
        User member2 = User.builder().id(3L).name("member2").build();
        List<User> fakeMemberList = List.of(member1, member2);

        String fakePresignedUrl = "https://s3.amazonaws.com/bucket/image-key.jpg?presigned-signature";

        given(userWorkspacePort.findUsersByWorkspaceId(workspace.getId())).willReturn(fakeMemberList);
        given(amazonS3Manager.generatePresignedUrl(workspace.getKeyName())).willReturn(fakePresignedUrl);

        // when
        WorkspaceResponseDTO.WorkspaceEditPage response = workspaceQueryUseCase.getEditPage(user, workspace);

        // then
        // 행위 검증: 의존 객체들이 올바른 인자로 호출되었는지 확인
        verify(userWorkspacePort).findUsersByWorkspaceId(workspace.getId());
        verify(amazonS3Manager).generatePresignedUrl(workspace.getKeyName());

        // 상태 검증: 반환된 DTO의 내용이 올바른지 확인
        assertThat(response.getImageUrl()).isEqualTo(fakePresignedUrl);
        assertThat(response.getMembers()).hasSize(2);
        assertThat(response.getMembers().get(0).getName()).isEqualTo(member1.getName());
        assertThat(response.getMembers().get(1).getName()).isEqualTo(member2.getName());
    }

    @Test
    @DisplayName("워크스페이스 편집 페이지 조회 성공 - 멤버 없음")
    void getEditPage_success_when_no_members() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder()
                .id(10L)
                .keyName("image-key.jpg")
                .build();

        String fakePresignedUrl = "https://s3.amazonaws.com/bucket/image-key.jpg?presigned-signature";

        given(userWorkspacePort.findUsersByWorkspaceId(workspace.getId())).willReturn(Collections.emptyList());
        given(amazonS3Manager.generatePresignedUrl(workspace.getKeyName())).willReturn(fakePresignedUrl);

        // when
        WorkspaceResponseDTO.WorkspaceEditPage response = workspaceQueryUseCase.getEditPage(user, workspace);

        // then
        assertThat(response.getImageUrl()).isEqualTo(fakePresignedUrl);
        assertThat(response.getMembers()).isNotNull();
        assertThat(response.getMembers()).isEmpty();
    }

    @Test
    @DisplayName("최근 문서 목록 조회 성공")
    void getRecentDocuments_success() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();

        UserDocumentLastOpened doc1 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 5L, DocumentType.AI_MEETING_MANAGER))
                .title("test document 1")
                .thumbnailKeyName("thumb1.jpg")
                .build();
        UserDocumentLastOpened doc2 = UserDocumentLastOpened.builder()
                .id(new UserDocumentId(user.getId(), 7L, DocumentType.SNS_EVENT_ASSISTANT))
                .title("test document 2")
                .thumbnailKeyName("thumb2.jpg")
                .build();
        List<UserDocumentLastOpened> fakeRecentDocumentList = List.of(doc1, doc2);

        given(userDocumentLastOpenedQueryUseCase.getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class)))
                .willReturn(fakeRecentDocumentList);
        given(amazonS3Manager.generatePresignedUrl("thumb1.jpg")).willReturn("presigned-url-1");
        given(amazonS3Manager.generatePresignedUrl("thumb2.jpg")).willReturn("presigned-url-2");

        // when
        WorkspaceResponseDTO.RecentDocumentList response = workspaceQueryUseCase.getDocumentsForMainPage(user, workspace);

        // then - 결과 검증
        // 상태 검증: 반환된 DTO의 크기와 내용(Presigned URL 포함) 확인
        assertThat(response.getDocuments()).hasSize(2);
        assertThat(response.getDocuments().get(0).getTitle()).isEqualTo(doc1.getTitle());
        assertThat(response.getDocuments().get(0).getThumbnailUrl()).isEqualTo("presigned-url-1");
        assertThat(response.getDocuments().get(1).getThumbnailUrl()).isEqualTo("presigned-url-2");

        // 행위 검증: 의존 객체들이 올바르게 호출되었는지 확인
        verify(userDocumentLastOpenedQueryUseCase).getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class));
        verify(amazonS3Manager, times(1)).generatePresignedUrl("thumb1.jpg");
        verify(amazonS3Manager, times(1)).generatePresignedUrl("thumb2.jpg");
    }

    @Test
    @DisplayName("최근 문서 목록 조회 - 결과 없음")
    void getRecentDocuments_returns_empty_list_when_no_documents() {

        // given
        User user = User.builder().id(1L).build();
        Workspace workspace = Workspace.builder().id(10L).build();

        given(userDocumentLastOpenedQueryUseCase.getRecentDocuments(eq(user.getId()), eq(workspace.getId()), any(PageRequest.class)))
                .willReturn(Collections.emptyList());

        // when
        WorkspaceResponseDTO.RecentDocumentList response = workspaceQueryUseCase.getDocumentsForMainPage(user, workspace);

        // then
        assertThat(response.getDocuments()).isNotNull();
        assertThat(response.getDocuments()).isEmpty();

        // S3 Manager는 한 번도 호출되지 않아야 함
        verify(amazonS3Manager, never()).generatePresignedUrl(anyString());
    }
}
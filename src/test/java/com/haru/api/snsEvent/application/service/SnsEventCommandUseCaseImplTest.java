package com.haru.api.snsEvent.application.service;

import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.snsEvent.application.port.in.UploadFileAndThumbnailUseCase;
import com.haru.api.snsEvent.application.port.in.WinnerDrawUseCase;
import com.haru.api.snsEvent.application.port.out.FilePort;
import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnsEventCommandUseCaseImplTest {

    @InjectMocks
    private SnsEventCommandUseCaseImpl snsEventCommandUseCase;

    @Mock
    private WorkspaceQueryUseCase workspaceQueryUseCase;
    @Mock
    private WinnerDrawUseCase winnerDrawUseCase;
    @Mock
    private SnsEventPort snsEventPort;
    @Mock
    private UploadFileAndThumbnailUseCase uploadFileAndThumbnailUseCase;
    @Mock
    private FilePort filePort;
    @Mock
    private UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;

    private User user;
    private User otherUser;
    private Workspace workspace;
    private SnsEvent snsEvent;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("testUser")
                .build();
        otherUser = User.builder()
                .id(2L)
                .name("otherUser")
                .build();

        workspace = Workspace.builder()
                .id(100L)
                .title("testWorkspace")
                .instagramAccessToken("access-token")
                .build();
        snsEvent = spy(SnsEvent.builder()
                .id(1L)
                .title("sns event 1")
                .workspace(workspace)
                .creator(user)
                .thumbnailKeyName("sns-event/100/thumbnail.jpg")
                .snsLink("https://snsevent.com")
                .snsLinkTitle("title")
                .build());
    }

    @Test
    @DisplayName("SNS 이벤트 생성 성공")
    void createSnsEvent_Success() {

        // given
        SnsEventRequestDTO.SnsCondition snsCondition = SnsEventRequestDTO.SnsCondition.builder()
                .winnerCount(3)
                .isPeriod(true)
                .period(LocalDateTime.now())
                .isKeyword(false)
                .keyword(null)
                .isTagged(false)
                .tagCount(0)
                .build();

        SnsEventRequestDTO.CreateSnsRequest request = SnsEventRequestDTO.CreateSnsRequest.builder()
                .title("test-title")
                .snsEventLink("https://instagram.com/p/shortcode")
                .snsCondition(snsCondition)
                .build();

        String expectedThumbnailKey = "sns-event/100/thumbnail.jpg";

        given(workspaceQueryUseCase.getWorkspace(workspace.getId())).willReturn(workspace);
        given(snsEventPort.save(any(SnsEvent.class))).willReturn(snsEvent);
        given(uploadFileAndThumbnailUseCase.createAndUploadListFileAndThumbnail(any(SnsEvent.class))).willReturn(expectedThumbnailKey);

        // when
        SnsEventResponseDTO.CreateSnsEventResponse response = snsEventCommandUseCase.createSnsEvent(user, workspace, request);

        // then
        assertThat(response).isNotNull();
        System.out.println(response.getSnsEventId());
        System.out.println(response.getId());
        assertThat(response.getSnsEventId()).isEqualTo(snsEvent.getId());

        verify(workspaceQueryUseCase, times(1)).getWorkspace(workspace.getId());
        verify(winnerDrawUseCase, times(1)).getAndSaveParticipantAndWinner(
                any(),
                eq(workspace.getInstagramAccessToken()),
                eq(request.getSnsEventLink()),
                eq(request.getSnsCondition())
        );
        verify(snsEventPort, times(1)).save(any(SnsEvent.class));
        verify(uploadFileAndThumbnailUseCase, times(1)).createAndUploadListFileAndThumbnail(any(SnsEvent.class));
        assertThat(snsEvent.getThumbnailKeyName()).isEqualTo(expectedThumbnailKey);
    }

    @Test
    @DisplayName("SNS 이벤트 제목 수정 성공")
    void updateSnsEvent_Success() {

        // given
        String newThumbnailKey = "new/thumbnail/key.jpg";
        String newTitle = "update-title";

        SnsEventRequestDTO.UpdateSnsEventRequest request = SnsEventRequestDTO.UpdateSnsEventRequest.builder()
                .title(newTitle)
                .thumbnailKeyName(newThumbnailKey)
                .build();
        UserWorkspace userWorkspace = UserWorkspace.builder()
                .user(user)
                .workspace(workspace)
                .build();

        given(snsEventPort.findById(snsEvent.getId())).willReturn(snsEvent);
        given(userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), snsEvent.getWorkspaceId()))
                .willReturn(Optional.of(userWorkspace));
        given(snsEventPort.save(any(SnsEvent.class))).willReturn(snsEvent);
        given(uploadFileAndThumbnailUseCase.createAndUploadListFileAndThumbnail(any(SnsEvent.class)))
                .willReturn(newThumbnailKey);

        // when
        snsEventCommandUseCase.updateSnsEvent(user, snsEvent, request);

        // then
        verify(snsEventPort, times(1)).findById(snsEvent.getId());
        verify(userWorkspaceQueryUseCase, times(1)).getUserWorkspace(user.getId(), snsEvent.getWorkspaceId());
        verify(filePort, times(1)).deleteSnsEventFileAndThumbnailImage(snsEvent);
        verify(uploadFileAndThumbnailUseCase, times(1)).createAndUploadListFileAndThumbnail(snsEvent);
        verify(snsEventPort, times(1)).save(snsEvent);

        verify(snsEvent, times(1)).updateTitle(newTitle);
        verify(snsEvent, times(1)).initThumbnailKeyName(newThumbnailKey);

        // 실제 객체의 상태가 변경되었는지 확인
        assertThat(snsEvent.getTitle()).isEqualTo(newTitle);
    }

    @Test
    @DisplayName("SNS 이벤트 제목 수정 실패 - 권한 없음")
    void updateSnsEvent_Fail_NoAuthority() {

        // given
        String newThumbnailKey = "new/thumbnail/key.jpg";
        String newTitle = "update-title";
        SnsEventRequestDTO.UpdateSnsEventRequest request = SnsEventRequestDTO.UpdateSnsEventRequest.builder()
                .title(newTitle)
                .thumbnailKeyName(newThumbnailKey)
                .build();

        UserWorkspace userWorkspaceForOtherUser = UserWorkspace.builder()
                .user(otherUser)
                .workspace(workspace)
                .build();

        given(snsEventPort.findById(snsEvent.getId())).willReturn(snsEvent);
        given(userWorkspaceQueryUseCase.getUserWorkspace(otherUser.getId(), snsEvent.getWorkspaceId()))
                .willReturn(Optional.of(userWorkspaceForOtherUser));

        // when & then
        assertThatThrownBy(() -> snsEventCommandUseCase.updateSnsEvent(otherUser, snsEvent, request))
                .isInstanceOf(SnsEventHandler.class)
                .hasMessageContaining("인스타그램 이벤트에 대한 수정 권한이 없습니다.");

        verify(snsEvent, never()).updateTitle(any(String.class));
        verify(snsEventPort, never()).save(any(SnsEvent.class));
        verify(filePort, never()).deleteSnsEventFileAndThumbnailImage(any(SnsEvent.class));
    }

    @Test
    @DisplayName("SNS 이벤트 삭제 성공")
    void deleteSnsEvent_Success() {

        // given
        UserWorkspace userWorkspace = UserWorkspace.builder().user(user).build();

        given(snsEventPort.findById(snsEvent.getId())).willReturn(snsEvent);
        given(userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), snsEvent.getWorkspaceId()))
                .willReturn(Optional.of(userWorkspace));

        // when
        snsEventCommandUseCase.deleteSnsEvent(user, snsEvent);

        // then
        InOrder inOrder = inOrder(filePort, snsEventPort);

        // S3 파일과 썸네일이 먼저 삭제되는지 확인
        inOrder.verify(filePort, times(1)).deleteSnsEventFileAndThumbnailImage(snsEvent);
        // DB에서 SNS 이벤트가 삭제되는지 확인
        inOrder.verify(snsEventPort, times(1)).delete(snsEvent);
    }

    @Test
    @DisplayName("SNS 이벤트 삭제 실패 - 권한 없음 (생성자가 아님)")
    void deleteSnsEvent_Fail_NoAuthority() {
        // given (준비)
        // otherUser는 워크스페이스 멤버이지만, snsEvent 생성자는 아님
        UserWorkspace otherUserWorkspace = UserWorkspace.builder().user(otherUser).build();

        given(snsEventPort.findById(snsEvent.getId())).willReturn(snsEvent);
        given(userWorkspaceQueryUseCase.getUserWorkspace(otherUser.getId(), snsEvent.getWorkspaceId()))
                .willReturn(Optional.of(otherUserWorkspace));

        // when & then
        assertThatThrownBy(() -> snsEventCommandUseCase.deleteSnsEvent(otherUser, snsEvent))
                .isInstanceOf(SnsEventHandler.class)
                .hasMessageContaining("인스타그램 이벤트에 대한 수정 권한이 없습니다.");

        verify(filePort, never()).deleteSnsEventFileAndThumbnailImage(any(SnsEvent.class));
        verify(snsEventPort, never()).delete(any(SnsEvent.class));
    }
}
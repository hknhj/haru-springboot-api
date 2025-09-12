package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.application.port.in.UploadFileAndThumbnailUseCase;
import com.haru.api.snsEvent.application.port.in.WinnerDrawUseCase;
import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        snsEvent = SnsEvent.builder()
                .id(1L)
                .title("sns event 1")
                .workspace(workspace)
                .creator(user)
                .snsLink("https://snsevent.com")
                .snsLinkTitle("title")
                .build();
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
        verify(uploadFileAndThumbnailUseCase, times(1)).createAndUploadListFileAndThumbnail(snsEvent);
        assertThat(snsEvent.getThumbnailKeyName()).isEqualTo(expectedThumbnailKey);
    }
}
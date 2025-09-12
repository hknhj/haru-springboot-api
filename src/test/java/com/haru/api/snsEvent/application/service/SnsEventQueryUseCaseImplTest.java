package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.application.port.out.WinnerPort;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SnsEventQueryUseCaseImplTest {

    @InjectMocks
    private SnsEventQueryUseCaseImpl snsEventQueryUseCase;

    @Mock
    private SnsEventPort snsEventPort;
    @Mock
    private ParticipantPort participantPort;
    @Mock
    private WinnerPort winnerPort;

    private User user;
    private Workspace workspace;
    private SnsEvent snsEvent;


    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        workspace = Workspace.builder().id(100L).build();
        snsEvent = SnsEvent.builder().id(10L).title("테스트 이벤트").build();
    }

    @Test
    @DisplayName("SNS 이벤트 목록 조회 성공")
    void getSnsEventList_Success() {

        // given
        SnsEvent event1 = SnsEvent.builder()
                .id(1L)
                .title("이벤트 1")
                .participantList(new ArrayList<>())
                .winnerList(new ArrayList<>())
                .build();
        SnsEvent event2 = SnsEvent.builder()
                .id(2L)
                .title("이벤트 2")
                .participantList(new ArrayList<>())
                .winnerList(new ArrayList<>())
                .build();
        List<SnsEvent> mockEventList = List.of(event1, event2);

        SnsEventResponseDTO.GetSnsEventListRequest mockResponse = SnsEventResponseDTO.GetSnsEventListRequest.builder()
                .snsEventList(
                        List.of(
                                SnsEventConverter.toSnsEventList(event1),
                                SnsEventConverter.toSnsEventList(event2)
                        )
                )
                .build();

        try (MockedStatic<SnsEventConverter> mockedConverter = Mockito.mockStatic(SnsEventConverter.class)) {

            given(snsEventPort.findAllByWorkspaceOrderByUpdatedAtDesc(workspace)).willReturn(mockEventList);

            mockedConverter.when(() -> SnsEventConverter.toGetSnsEventListRequest(mockEventList))
                    .thenReturn(mockResponse);

            // when
            SnsEventResponseDTO.GetSnsEventListRequest response = snsEventQueryUseCase.getSnsEventList(user, workspace);

            // then
            verify(snsEventPort).findAllByWorkspaceOrderByUpdatedAtDesc(workspace);
            mockedConverter.verify(() -> SnsEventConverter.toGetSnsEventListRequest(mockEventList));

            assertThat(response).isNotNull();
            assertThat(response.getSnsEventList()).hasSize(2);
            assertThat(response.getSnsEventList().get(0).getTitle()).isEqualTo("이벤트 1");
        }
    }

    @Test
    @DisplayName("SNS 이벤트 목록이 없을 경우 빈 리스트 반환")
    void getSnsEventList_WhenEmpty() {

        // given
        given(snsEventPort.findAllByWorkspaceOrderByUpdatedAtDesc(workspace))
                .willReturn(Collections.emptyList());

        SnsEventResponseDTO.GetSnsEventListRequest mockResponse = SnsEventResponseDTO.GetSnsEventListRequest.builder()
                .snsEventList(Collections.emptyList())
                .build();

        try (MockedStatic<SnsEventConverter> mockedConverter = Mockito.mockStatic(SnsEventConverter.class)) {
            mockedConverter.when(() -> SnsEventConverter.toGetSnsEventListRequest(any()))
                    .thenReturn(mockResponse);

            // when
            SnsEventResponseDTO.GetSnsEventListRequest response = snsEventQueryUseCase.getSnsEventList(user, workspace);

            // then
            verify(snsEventPort).findAllByWorkspaceOrderByUpdatedAtDesc(workspace);

            assertThat(response).isNotNull();
            assertThat(response.getSnsEventList()).isEmpty();
        }
    }

    @Test
    @DisplayName("SNS 이벤트 상세 정보 조회 성공")
    void getSnsEvent_Success() {

        // given
        List<Participant> mockParticipantList = List.of(Participant.builder().id(1L).nickname("참여자1").build());
        List<Winner> mockWinnerList = List.of(Winner.builder().id(1L).nickname("당첨자1").build());

        SnsEventResponseDTO.GetSnsEventRequest mockResponse = SnsEventResponseDTO.GetSnsEventRequest.builder()
                .title(snsEvent.getTitle())
                .participantList(List.of(SnsEventResponseDTO.ParticipantResponse.builder().account("test-account").build()))
                .winnerList(List.of(SnsEventResponseDTO.WinnerResponse.builder().account("winner-account").build()))
                .build();

        try (MockedStatic<SnsEventConverter> mockedConverter = Mockito.mockStatic(SnsEventConverter.class)) {
            given(participantPort.findAllBySnsEventId(snsEvent.getId())).willReturn(mockParticipantList);
            given(winnerPort.findAllBySnsEventId(snsEvent.getId())).willReturn(mockWinnerList);

            mockedConverter.when(() -> SnsEventConverter.toGetSnsEventRequest(snsEvent, mockParticipantList, mockWinnerList))
                    .thenReturn(mockResponse);

            // when
            SnsEventResponseDTO.GetSnsEventRequest response = snsEventQueryUseCase.getSnsEvent(user, snsEvent);

            // then
            verify(participantPort).findAllBySnsEventId(snsEvent.getId());
            verify(winnerPort).findAllBySnsEventId(snsEvent.getId());

            mockedConverter.verify(() -> SnsEventConverter.toGetSnsEventRequest(snsEvent, mockParticipantList, mockWinnerList));

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("테스트 이벤트");
            assertThat(response.getParticipantList()).hasSize(1);
            assertThat(response.getWinnerList()).hasSize(1);
        }
    }

    @Test
    @DisplayName("참여자나 당첨자가 없을 경우 빈 리스트로 조회")
    void getSnsEvent_WhenListsAreEmpty() {
        // given (준비)
        // Port들이 빈 리스트를 반환하도록 설정
        List<Participant> emptyParticipantList = Collections.emptyList();
        List<Winner> emptyWinnerList = Collections.emptyList();

        // Converter가 반환할 DTO
        SnsEventResponseDTO.GetSnsEventRequest mockResponse = SnsEventResponseDTO.GetSnsEventRequest.builder()
                .title(snsEvent.getTitle())
                .participantList(Collections.emptyList())
                .winnerList(Collections.emptyList())
                .build();

        try (MockedStatic<SnsEventConverter> mockedConverter = Mockito.mockStatic(SnsEventConverter.class)) {
            given(participantPort.findAllBySnsEventId(snsEvent.getId())).willReturn(emptyParticipantList);
            given(winnerPort.findAllBySnsEventId(snsEvent.getId())).willReturn(emptyWinnerList);
            mockedConverter.when(() -> SnsEventConverter.toGetSnsEventRequest(snsEvent, emptyParticipantList, emptyWinnerList))
                    .thenReturn(mockResponse);

            // when (실행)
            SnsEventResponseDTO.GetSnsEventRequest response = snsEventQueryUseCase.getSnsEvent(user, snsEvent);

            // then (검증)
            verify(participantPort).findAllBySnsEventId(snsEvent.getId());
            verify(winnerPort).findAllBySnsEventId(snsEvent.getId());

            // 반환된 DTO의 리스트들이 비어있는지 확인
            assertThat(response).isNotNull();
            assertThat(response.getParticipantList()).isEmpty();
            assertThat(response.getWinnerList()).isEmpty();
        }
    }

}
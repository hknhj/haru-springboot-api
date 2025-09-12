package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.domain.SnsEvent;
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

    private User user;
    private Workspace workspace;


    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        workspace = Workspace.builder().id(100L).build();
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
            // Port 메서드가 호출되면 mockEventList를 반환하도록 설정
            given(snsEventPort.findAllByWorkspaceOrderByUpdatedAtDesc(workspace)).willReturn(mockEventList);

            // Converter의 정적 메서드가 호출되면 mockResponse를 반환하도록 설정
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

}
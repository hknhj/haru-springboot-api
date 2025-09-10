package com.haru.api.meeting.application.service;

import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingQueryUseCaseImplTest {

    @InjectMocks
    private MeetingQueryUseCaseImpl meetingQueryUseCase;

    @Mock
    private MeetingPort meetingPort;

    private User user1;
    private User user2;
    private Workspace workspace;
    private Meeting meeting1;
    private Meeting meeting2;
    private List<Meeting> meetings;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("testUser")
                .build();

        user2 = User.builder()
                .id(5L)
                .name("testCreator")
                .build();

        workspace = Workspace.builder()
                .id(100L)
                .title("testWorkspace")
                .build();

        meeting1 = Meeting.builder()
                .id(1L)
                .title("회의1")
                .workspace(workspace)
                .creator(user1)
                .build();
        meeting2 = Meeting.builder()
                .id(2L)
                .title("회의2")
                .workspace(workspace)
                .creator(user2)
                .build();
        meetings = List.of(meeting1, meeting2);
    }

    @Test
    @DisplayName("회의 목록 조회 성공")
    void getMeetings_Success() {

        // given
        // 반환될 DTO 객체
        MeetingResponseDTO.getMeetingResponse response1 = MeetingResponseDTO.getMeetingResponse.builder()
                .meetingId(1L)
                .title(meeting1.getTitle())
                .isCreator(true)
                .build();
        MeetingResponseDTO.getMeetingResponse response2 = MeetingResponseDTO.getMeetingResponse.builder()
                .meetingId(2L)
                .title(meeting2.getTitle())
                .isCreator(false)
                .build();
        List<MeetingResponseDTO.getMeetingResponse> expectedResponses = List.of(response1, response2);

        // mock 객체 동작 정의
        given(meetingPort.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId())).willReturn(meetings);

        // MeetingConverter의 정적(static) 메서드 모킹
        try (MockedStatic<MeetingConverter> mockedConverter = mockStatic(MeetingConverter.class)) {
            mockedConverter.when(() -> MeetingConverter.toGetMeetingResponse(meeting1, user1.getId())).thenReturn(response1);
            mockedConverter.when(() -> MeetingConverter.toGetMeetingResponse(meeting2, user1.getId())).thenReturn(response2);

            // when
            List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetings(user1, workspace);

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result).isEqualTo(expectedResponses);

            // Mock 객체가 예상대로 호출되었는지 검증
            verify(meetingPort, times(1)).findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingResponse(meeting1, user1.getId()), times(1));
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingResponse(meeting2, user1.getId()), times(1));
        }
    }

    @Test
    @DisplayName("회의 목록 조회 성공 - 데이터 없음")
    void getMeetings_WhenNoMeetings_ReturnsEmptyList() {

        // given
        given(meetingPort.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId())).willReturn(Collections.emptyList());

        // when
        List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetings(user1, workspace);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(meetingPort, times(1)).findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());
    }

    @Test
    @DisplayName("회의록 상세 조회 성공")
    void getMeetingProceeding_Success() {

        // given
        MeetingResponseDTO.getMeetingProceeding expectedResponse = MeetingResponseDTO.getMeetingProceeding.builder()
                .userId(user1.getId())
                .workspaceId(workspace.getId())
                .build();

        try (MockedStatic<MeetingConverter> mockedConverter = mockStatic(MeetingConverter.class)) {
            mockedConverter.when(() -> MeetingConverter.toGetMeetingProceedingResponse(user1, meeting1))
                    .thenReturn(expectedResponse);

            // when
            MeetingResponseDTO.getMeetingProceeding result = meetingQueryUseCase.getMeetingProceeding(user1, meeting1);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse); // 반환된 결과가 기대한 DTO와 일치하는지 확인

            // Mock 객체들이 예상대로 호출되었는지 검증
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingProceedingResponse(user1, meeting1), times(1));
        }
    }
}
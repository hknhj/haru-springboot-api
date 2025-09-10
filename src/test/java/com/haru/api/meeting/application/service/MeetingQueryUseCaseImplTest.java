package com.haru.api.meeting.application.service;

import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingQueryUseCaseImplTest {

    @InjectMocks
    private MeetingQueryUseCaseImpl meetingQueryUseCase;

    @Mock
    private MeetingPort meetingPort;

    @Test
    @DisplayName("회의 목록 조회 성공")
    void getMeetings_Success() {

        // given

        // 테스트용 데이터
        User user = User.builder()
                .id(1L)
                .name("testUser")
                .build();

        Workspace workspace = Workspace.builder()
                .id(100L)
                .title("testWorkspace")
                .build();

        Meeting meeting1 = Meeting.builder()
                .id(1L)
                .title("회의1")
                .workspace(workspace)
                .build();
        Meeting meeting2 = Meeting.builder()
                .id(2L)
                .title("회의2")
                .workspace(workspace)
                .build();
        List<Meeting> meetings = List.of(meeting1, meeting2);

        // 반환될 DTO 객체
        MeetingResponseDTO.getMeetingResponse response1 = MeetingResponseDTO.getMeetingResponse.builder()
                .meetingId(1L)
                .title("회의1")
                .isCreator(true)
                .build();
        MeetingResponseDTO.getMeetingResponse response2 = MeetingResponseDTO.getMeetingResponse.builder()
                .meetingId(2L)
                .title("회의2")
                .isCreator(false)
                .build();
        List<MeetingResponseDTO.getMeetingResponse> expectedResponses = List.of(response1, response2);

        // mock 객체 동작 정의
        when(meetingPort.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId())).thenReturn(meetings);

        // MeetingConverter의 정적(static) 메서드 모킹
        try (MockedStatic<MeetingConverter> mockedConverter = mockStatic(MeetingConverter.class)) {
            mockedConverter.when(() -> MeetingConverter.toGetMeetingResponse(meeting1, user.getId())).thenReturn(response1);
            mockedConverter.when(() -> MeetingConverter.toGetMeetingResponse(meeting2, user.getId())).thenReturn(response2);

            // when
            List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetings(user, workspace);

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result).isEqualTo(expectedResponses);

            // Mock 객체가 예상대로 호출되었는지 검증
            verify(meetingPort, times(1)).findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingResponse(meeting1, user.getId()), times(1));
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingResponse(meeting2, user.getId()), times(1));
        }
    }

    @Test
    @DisplayName("회의 목록 조회 성공 - 데이터 없음")
    void getMeetings_WhenNoMeetings_ReturnsEmptyList() {

        // given
        User user = User.builder()
                .id(1L)
                .name("testUser")
                .build();

        Workspace workspace = Workspace.builder()
                .id(100L)
                .title("testWorkspace")
                .build();

        when(meetingPort.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId())).thenReturn(Collections.emptyList());

        // when
        List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetings(user, workspace);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(meetingPort, times(1)).findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());
    }
}
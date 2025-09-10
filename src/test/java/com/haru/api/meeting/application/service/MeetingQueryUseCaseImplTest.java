package com.haru.api.meeting.application.service;

import com.haru.api.global.apiPayload.exception.handler.MeetingHandler;
import com.haru.api.infra.api.entity.SpeechSegment;
import com.haru.api.infra.api.repository.SpeechSegmentRepository;
import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.snsEvent.domain.enums.Format;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingQueryUseCaseImplTest {

    @InjectMocks
    private MeetingQueryUseCaseImpl meetingQueryUseCase;

    @Mock
    private MeetingPort meetingPort;
    @Mock
    private SpeechSegmentRepository speechSegmentRepository;
    @Mock
    private AmazonS3Manager amazonS3Manager;

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
                .startTime(LocalDateTime.of(2025, 9, 9, 14, 0))
                .build();
        meeting2 = Meeting.builder()
                .id(2L)
                .title("회의2")
                .workspace(workspace)
                .creator(user2)
                .startTime(LocalDateTime.of(2025, 9, 10, 14, 0))
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
            List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetingList(user1, workspace);

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
        List<MeetingResponseDTO.getMeetingResponse> result = meetingQueryUseCase.getMeetingList(user1, workspace);

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
            assertThat(result).isEqualTo(expectedResponse);

            // Mock 객체들이 예상대로 호출되었는지 검증
            mockedConverter.verify(() -> MeetingConverter.toGetMeetingProceedingResponse(user1, meeting1), times(1));
        }
    }

    @Test
    @DisplayName("스크립트 조회 성공")
    void getTranscript_Success() {

        // given
        SpeechSegment segment1 = SpeechSegment.builder()
                .id(1L)
                .meeting(meeting1)
                .speakerId("user1")
                .text("첫 번째 발언입니다.")
                .build();
        SpeechSegment segment2 = SpeechSegment.builder()
                .id(2L)
                .meeting(meeting1)
                .speakerId("user2")
                .text("두 번째 발언입니다.")
                .build();
        List<SpeechSegment> segments = List.of(segment1, segment2);

        given(speechSegmentRepository.findAllByMeetingIdWithAIQuestions(meeting1.getId())).willReturn(segments);

        // when
        MeetingResponseDTO.TranscriptResponse result = meetingQueryUseCase.getTranscript(user1, meeting1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMeetingStartTime()).isEqualTo(meeting1.getStartTime());
        assertThat(result.getTranscripts()).hasSize(2);

        // DTO 변환이 잘 되었는지 일부 필드 확인
        MeetingResponseDTO.Transcript transcript1 = result.getTranscripts().get(0);
        assertThat(transcript1.getSegmentId()).isEqualTo(segment1.getId());
        assertThat(transcript1.getText()).isEqualTo(segment1.getText());
        assertThat(transcript1.getAiQuestions()).isEmpty();

        MeetingResponseDTO.Transcript transcript2 = result.getTranscripts().get(1);
        assertThat(transcript2.getSegmentId()).isEqualTo(segment2.getId());
        assertThat(transcript2.getAiQuestions()).isEmpty();

        verify(speechSegmentRepository, times(1)).findAllByMeetingIdWithAIQuestions(meeting1.getId());
    }

    @Test
    @DisplayName("스크립트가 없을 경우 빈 리스트 반환")
    void getTranscript_WhenNoSegments_ReturnsEmptyList() {

        // given
        given(speechSegmentRepository.findAllByMeetingIdWithAIQuestions(meeting1.getId())).willReturn(Collections.emptyList());

        // when
        MeetingResponseDTO.TranscriptResponse result = meetingQueryUseCase.getTranscript(user1, meeting1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMeetingStartTime()).isEqualTo(meeting1.getStartTime());
        assertThat(result.getTranscripts()).isNotNull();
        assertThat(result.getTranscripts()).isEmpty();

        // repository가 정확히 1번 호출되었는지 검증
        verify(speechSegmentRepository, times(1)).findAllByMeetingIdWithAIQuestions(meeting1.getId());
    }

    @Test
    @DisplayName("PDF 파일 다운로드 링크 생성 성공")
    void downloadMeeting_forPdf_Success() {

        // given
        Meeting meeting = mock(Meeting.class);
        String pdfKey = "proceedings/meeting-1.pdf";
        String expectedUrl = "https://s3.presigned.url/for/pdf";
        Format format = Format.PDF;

        given(meeting.getProceedingPdfKeyName()).willReturn(pdfKey);
        given(amazonS3Manager.generatePresignedUrl(pdfKey)).willReturn(expectedUrl);

        // when
        MeetingResponseDTO.proceedingDownLoadLinkResponse result = meetingQueryUseCase.downloadMeeting(user1, meeting, format);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDownloadLink()).isEqualTo(expectedUrl);

        // Mock 객체 호출 검증
        verify(meeting, times(1)).getProceedingPdfKeyName();
        verify(meeting, never()).getProceedingWordKeyName();
        verify(amazonS3Manager, times(1)).generatePresignedUrl(pdfKey);
    }

    @Test
    @DisplayName("DOCX 파일 다운로드 링크 생성 성공")
    void downloadMeeting_forDocx_Success() {

        // given
        Meeting meeting = mock(Meeting.class);
        String wordKey = "proceedings/meeting-1.docx";
        String expectedUrl = "https://s3.presigned.url/for/docx";
        Format format = Format.DOCX;

        given(meeting.getProceedingWordKeyName()).willReturn(wordKey);
        given(amazonS3Manager.generatePresignedUrl(wordKey)).willReturn(expectedUrl);

        // when
        MeetingResponseDTO.proceedingDownLoadLinkResponse result = meetingQueryUseCase.downloadMeeting(user1, meeting, format);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDownloadLink()).isEqualTo(expectedUrl);

        verify(meeting, times(1)).getProceedingWordKeyName();
        verify(meeting, never()).getProceedingPdfKeyName();
        verify(amazonS3Manager, times(1)).generatePresignedUrl(wordKey);
    }

    @Test
    @DisplayName("파일 키가 존재하지 않거나 비어있을 경우 예외 발생")
    void downloadMeeting_KeyNotFoundOrBlank_ThrowsException() {

        // given
        Meeting meeting = mock(Meeting.class);
        Format format = mock(Format.class);
        when(meeting.getProceedingPdfKeyName()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> meetingQueryUseCase.downloadMeeting(user1, meeting, format))
                .isInstanceOf(MeetingHandler.class)
                .hasMessageContaining("AI회의록이 없습니다");

        verify(amazonS3Manager, never()).generatePresignedUrl(anyString());
    }

    @Test
    @DisplayName("음성 파일 다운로드 링크 생성 성공")
    void getMeetingVoiceFile_Success() {

        // given
        Meeting meeting = mock(Meeting.class);
        String audioKey = "audios/meeting-1.m4a";
        String expectedUrl = "https://s3.presigned.url/for/audio";
        given(meeting.getAudioFileKey()).willReturn(audioKey);
        given(amazonS3Manager.generatePresignedUrl(audioKey)).willReturn(expectedUrl);

        // when
        MeetingResponseDTO.proceedingVoiceLinkResponse result = meetingQueryUseCase.getMeetingVoiceFile(user1, meeting);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVoiceLink()).isEqualTo(expectedUrl);
        verify(meeting, times(1)).getAudioFileKey();
        verify(amazonS3Manager, times(1)).generatePresignedUrl(audioKey);
    }

    @Test
    @DisplayName("음성 파일 키가 존재하지 않거나 비어있을 경우 예외 발생")
    void getMeetingVoiceFile_KeyNotFoundOrBlank_ThrowsException() {

        // given
        Meeting meeting = mock(Meeting.class);
        String blankKey = "";
        given(meeting.getAudioFileKey()).willReturn(blankKey);

        // when & then
        assertThatThrownBy(() -> meetingQueryUseCase.getMeetingVoiceFile(user1, meeting))
                .isInstanceOf(MeetingHandler.class)
                .hasMessageContaining("AI회의록이 없습니다");

        verify(amazonS3Manager,never()).generatePresignedUrl(anyString());
    }

    @Test
    @DisplayName("워크스페이스의 모든 회의 목록 조회 성공")
    void getAllMeetingsInWorkspace_Success() {

        // given
        given(meetingPort.findAllByWorkspaceId(workspace.getId())).willReturn(meetings);

        // when
        List<Meeting> result = meetingQueryUseCase.getAllMeetingsInWorkspace(workspace.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(meetings);
        verify(meetingPort, times(1)).findAllByWorkspaceId(workspace.getId());
    }

    @Test
    @DisplayName("캘린더용 특정 기간의 회의 목록 조회 성공")
    void getAllMeetingsForCalendar_Success() {

        // given
        LocalDateTime startDate = LocalDateTime.of(2025, 9, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 9, 30, 23, 59);

        given(meetingPort.findAllForCalendars(workspace.getId(), startDate, endDate)).willReturn(meetings);

        // when
        List<Meeting> result = meetingQueryUseCase.getAllMeetingsForCalendar(workspace.getId(), startDate, endDate);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(meetings);
        verify(meetingPort, times(1)).findAllForCalendars(workspace.getId(), startDate, endDate);
    }

}
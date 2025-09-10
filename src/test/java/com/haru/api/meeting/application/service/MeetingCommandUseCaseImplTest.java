package com.haru.api.meeting.application.service;

import com.haru.api.global.application.port.FileExtractorService;
import com.haru.api.infra.api.client.ChatGPTClient;
import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.application.port.out.AudioUploadPort;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.presentation.dto.MeetingRequestDTO;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingCommandUseCaseImplTest {

    @InjectMocks
    private MeetingCommandUseCaseImpl meetingCommandUseCase;

    @Mock
    private AmazonS3Manager amazonS3Manager;
    @Mock
    private MeetingPort meetingPort;
    @Mock
    private ChatGPTClient chatGPTClient;
    @Mock
    private FileExtractorService fileExtractorService;
    @Mock
    private AudioUploadPort audioUploadPort;

    private User user;
    private Workspace workspace;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("testUser")
                .build();

        workspace = Workspace.builder()
                .id(100L)
                .title("testWorkspace")
                .build();

        meeting = Meeting.builder()
                .id(1L)
                .title("회의1")
                .workspace(workspace)
                .creator(user)
                .agendaResult("")
                .startTime(LocalDateTime.of(2025, 9, 9, 14, 0))
                .build();
    }

    @Test
    @DisplayName("회의 생성 성공")
    void createMeeting_Success() {

        // given
        MeetingRequestDTO.createMeetingRequest request = new MeetingRequestDTO.createMeetingRequest("새로운 회의 제목");
        MultipartFile agendaFile = mock(MultipartFile.class);
        String extractedText = "이것은 파일에서 추출된 텍스트입니다.";
        String agendaSummary = "AI가 요약한 안건 내용입니다.";

        MeetingResponseDTO.createMeetingResponse expectedResponse = MeetingResponseDTO.createMeetingResponse.builder()
                .meetingId(meeting.getId())
                .title("created meeting")
                .build();

        given(fileExtractorService.extractTextFromFile(agendaFile)).willReturn(extractedText);
        given(chatGPTClient.summarizeDocument(extractedText)).willReturn(Mono.just(agendaSummary));
        given(meetingPort.save(any(Meeting.class))).willReturn(meeting);

        // 정적 메서드 Mocking
        try (MockedStatic<MeetingConverter> mockedConverter = mockStatic(MeetingConverter.class)) {
            mockedConverter.when(() -> MeetingConverter.toCreateMeetingResponse(meeting)).thenReturn(expectedResponse);

            // when
            MeetingResponseDTO.createMeetingResponse result = meetingCommandUseCase.createMeeting(user, workspace, agendaFile, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMeetingId()).isEqualTo(expectedResponse.getMeetingId());

            // MeetingPort.save()에 전달된 Meeting 객체를 캡처
            ArgumentCaptor<Meeting> meetingCaptor = ArgumentCaptor.forClass(Meeting.class);
            verify(meetingPort, times(1)).save(meetingCaptor.capture());

            // 캡처된 객체의 내용 검증
            Meeting capturedMeeting = meetingCaptor.getValue();
            assertThat(capturedMeeting.getTitle()).isEqualTo(request.getTitle());
            assertThat(capturedMeeting.getAgendaResult()).isEqualTo(agendaSummary);
            assertThat(capturedMeeting.getCreator()).isEqualTo(user);
            assertThat(capturedMeeting.getWorkspace()).isEqualTo(workspace);

            // 다른 Mock 객체들도 올바르게 호출되었는지 검증
            verify(fileExtractorService, times(1)).extractTextFromFile(agendaFile);
            verify(chatGPTClient, times(1)).summarizeDocument(extractedText);
            mockedConverter.verify(() -> MeetingConverter.toCreateMeetingResponse(meeting), times(1));
        }
    }

}
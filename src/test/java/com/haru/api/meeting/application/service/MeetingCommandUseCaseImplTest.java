package com.haru.api.meeting.application.service;

import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.global.application.port.FileExtractorService;
import com.haru.api.infra.api.client.ChatGPTClient;
import com.haru.api.infra.api.entity.SpeechSegment;
import com.haru.api.infra.api.repository.SpeechSegmentRepository;
import com.haru.api.infra.s3.MarkdownFileUploader;
import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.presentation.dto.MeetingRequestDTO;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.domain.enums.Auth;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingCommandUseCaseImplTest {

    @InjectMocks
    private MeetingCommandUseCaseImpl meetingCommandUseCase;

    @Mock
    private MeetingPort meetingPort;
    @Mock
    private ChatGPTClient chatGPTClient;
    @Mock
    private FileExtractorService fileExtractorService;
    @Mock
    private MarkdownFileUploader markdownFileUploader;
    @Mock
    private UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;
    @Mock
    private SpeechSegmentRepository speechSegmentRepository;
    @Mock
    private UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    private User user;
    private User otherUser;
    private Workspace workspace;
    private Meeting meeting;

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
                .build();

        meeting = spy(Meeting.builder()
                .id(1L)
                .title("회의1")
                .workspace(workspace)
                .creator(user)
                .agendaResult("")
                .startTime(LocalDateTime.of(2025, 9, 9, 14, 0))
                .build());
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

            verify(fileExtractorService, times(1)).extractTextFromFile(agendaFile);
            verify(chatGPTClient, times(1)).summarizeDocument(extractedText);
            mockedConverter.verify(() -> MeetingConverter.toCreateMeetingResponse(meeting), times(1));
        }
    }

    @Test
    @DisplayName("회의 제목 변경 성공")
    void updateMeetingTitle_Success() {

        // given
        String newTitle = "변경된 회의 제목";
        MeetingRequestDTO.updateTitle request = MeetingRequestDTO.updateTitle.builder()
                .title(newTitle)
                .build();

        UserWorkspace userWorkspace = UserWorkspace.builder()
                .id(1L)
                .auth(Auth.ADMIN)
                .build();

        String pdfKey = "meeting/proceeding.pdf";
        String wordKey = "meeting/proceeding.docx";

        given(meetingPort.findById(meeting.getId())).willReturn(Optional.of(meeting));

        given(meeting.getCreator()).willReturn(user);
        given(meeting.getProceedingPdfKeyName()).willReturn(pdfKey);
        given(meeting.getProceedingWordKeyName()).willReturn(wordKey);

        given(userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), workspace.getId()))
                .willReturn(Optional.of(userWorkspace));

        // when
        meetingCommandUseCase.updateMeetingTitle(user, meeting, request);

        // then
        verify(meeting, times(1)).updateTitle(newTitle);
        verify(markdownFileUploader, times(1)).updateFileTitle(pdfKey, newTitle + ".pdf");
        verify(markdownFileUploader, times(1)).updateFileTitle(wordKey, newTitle + ".docx");

        verify(meetingPort, times(1)).save(meeting);
    }

    @Test
    @DisplayName("회의 생성자가 아닌 경우 제목 변경 시 권한 없음 예외 발생")
    void updateMeetingTitle_NoAuthority_ThrowsException() {

        // given
        String newTitle = "변경된 회의 제목";
        MeetingRequestDTO.updateTitle request = MeetingRequestDTO.updateTitle.builder()
                        .title(newTitle)
                        .build();

        UserWorkspace userWorkspace = UserWorkspace.builder()
                        .id(1L)
                        .auth(Auth.MEMBER)
                        .build();

        given(meeting.getCreator()).willReturn(otherUser);
        given(meetingPort.findById(any(Long.class))).willReturn(Optional.of(meeting));
        given(userWorkspaceQueryUseCase.getUserWorkspace(otherUser.getId(), meeting.getWorkspace().getId())).willReturn(Optional.of(userWorkspace));

        // when & then
        assertThatThrownBy(() -> meetingCommandUseCase.updateMeetingTitle(otherUser, meeting, request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("수정 및 삭제할 권한이 없습니다.");

        verify(meeting, never()).updateTitle(anyString());
        verify(markdownFileUploader, never()).updateFileTitle(anyString(), anyString());
        verify(meetingPort, never()).save(meeting);
    }

    @Test
    @DisplayName("회의 삭제 성공 - 요청자가 생성자일 경우")
    void deleteMeeting_Success_ByCreator() {

        // given
        Meeting meeting = mock(Meeting.class);
        UserWorkspace userWorkspace = UserWorkspace.builder()
                .user(user)
                .workspace(workspace)
                .auth(Auth.ADMIN)
                .build();

        given(meeting.getId()).willReturn(101L);
        given(meeting.getCreator()).willReturn(user);
        given(meeting.getWorkspace()).willReturn(workspace);
        given(meetingPort.findById(meeting.getId())).willReturn(Optional.of(meeting));
        given(userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), workspace.getId()))
                .willReturn(Optional.of(userWorkspace));

        SpeechSegment segment = SpeechSegment.builder()
                .id(1L)
                .meeting(meeting)
                .speakerId("user1")
                .text("첫 번째 발언입니다.")
                .build();
        List<SpeechSegment> segments = List.of(segment);
        given(speechSegmentRepository.findByMeeting(meeting)).willReturn(segments);

        // when
        meetingCommandUseCase.deleteMeeting(user, meeting);

        // then
        verify(markdownFileUploader, times(4)).deleteS3File(any());
        verify(speechSegmentRepository, times(1)).deleteAll(segments);
        verify(meetingPort, times(1)).delete(meeting);
    }

    @Test
    @DisplayName("회의 삭제 실패 - 권한 없는 사용자")
    void deleteMeeting_NoAuthority_ThrowsException() {
        // given
        Meeting meeting = mock(Meeting.class);
        UserWorkspace otherUserWorkspace = UserWorkspace.builder()
                .user(otherUser)
                .workspace(workspace)
                .auth(Auth.MEMBER)
                .build();

        given(meeting.getId()).willReturn(101L);
        given(meeting.getCreator()).willReturn(user);
        given(meeting.getWorkspace()).willReturn(workspace);
        given(meetingPort.findById(meeting.getId())).willReturn(Optional.of(meeting));
        given(userWorkspaceQueryUseCase.getUserWorkspace(otherUser.getId(), workspace.getId()))
                .willReturn(Optional.of(otherUserWorkspace));

        // when & then
        assertThatThrownBy(() -> meetingCommandUseCase.deleteMeeting(otherUser, meeting))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("수정 및 삭제할 권한이 없습니다.");

        // 예외 발생 시, 어떤 삭제 로직도 호출되면 안됨
        verifyNoInteractions(markdownFileUploader);
        verify(speechSegmentRepository, never()).deleteAll(any());
        verify(meetingPort, never()).delete(any());
    }

    @Test
    @DisplayName("회의록 수정 성공 - 요청자가 생성자일 경우")
    void adjustProceeding_Success_ByCreator() {

        // given
        Meeting foundMeeting = mock(Meeting.class);
        String newProceedingText = "이것은 수정된 회의록입니다.";
        MeetingRequestDTO.meetingProceedingRequest request = MeetingRequestDTO.meetingProceedingRequest.builder()
                .proceeding(newProceedingText)
                .build();

        UserWorkspace userWorkspace = UserWorkspace.builder()
                .user(user)
                .workspace(workspace)
                .auth(Auth.ADMIN)
                .build();

        UserDocumentLastOpened lastOpened = mock(UserDocumentLastOpened.class);
        List<UserDocumentLastOpened> lastOpenedList = List.of(lastOpened);
        String newThumbnailKey = "new/thumbnail.jpg";

        given(meetingPort.findById(user.getId())).willReturn(Optional.of(foundMeeting));
        given(foundMeeting.getWorkspace()).willReturn(workspace);
        given(foundMeeting.getCreator()).willReturn(user);
        given(userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), workspace.getId()))
                .willReturn(Optional.of(userWorkspace));

        // 파일 업로드 관련 Mocking
        given(markdownFileUploader.createOrUpdatePdf(anyString(), anyString(), any(), anyString())).willReturn("new/pdf.key");
        given(markdownFileUploader.createOrUpdateWord(anyString(), anyString(), any(), anyString())).willReturn("new/word.key");
        given(markdownFileUploader.createOrUpdateThumbnail(anyString(), anyString(), any())).willReturn(newThumbnailKey);
        given(userDocumentLastOpenedQueryUseCase.getDocumentAccessHistory(any(), any())).willReturn(lastOpenedList);

        // when
        meetingCommandUseCase.adjustProceeding(user, meeting, request);

        // then
        verify(foundMeeting, times(1)).updateProceeding(newProceedingText);
        verify(markdownFileUploader, times(1)).createOrUpdatePdf(anyString(), anyString(), any(), anyString());
        verify(markdownFileUploader, times(1)).createOrUpdateWord(anyString(), anyString(), any(), anyString());
        verify(markdownFileUploader, times(1)).createOrUpdateThumbnail(anyString(), anyString(), any());
        verify(lastOpened, times(1)).updateThumbnailKeyName(newThumbnailKey);
    }

    @Test
    @DisplayName("회의록 수정 실패 - 권한 없는 사용자")
    void adjustProceeding_NoAuthority_ThrowsException() {

        // given
        Meeting foundMeeting = mock(Meeting.class);
        MeetingRequestDTO.meetingProceedingRequest request = MeetingRequestDTO.meetingProceedingRequest.builder()
                .proceeding("some text")
                .build();

        UserWorkspace otherUserWorkspace = UserWorkspace.builder()
                .user(otherUser)
                .workspace(workspace)
                .auth(Auth.MEMBER)
                .build();

        given(meetingPort.findById(meeting.getId())).willReturn(Optional.of(foundMeeting));
        given(foundMeeting.getWorkspace()).willReturn(workspace);
        given(foundMeeting.getCreator()).willReturn(user);
        given(userWorkspaceQueryUseCase.getUserWorkspace(otherUser.getId(), workspace.getId()))
                .willReturn(Optional.of(otherUserWorkspace));

        // when & then
        assertThatThrownBy(() -> meetingCommandUseCase.adjustProceeding(otherUser, meeting, request))
                .isInstanceOf(MemberHandler.class)
                .hasMessageContaining("수정 및 삭제할 권한이 없습니다.");

        // 예외 발생 시, 회의록 내용이나 파일 관련 메서드는 호출되지 않아야 함
        verify(foundMeeting, never()).updateProceeding(anyString());
        verifyNoInteractions(markdownFileUploader);
        verifyNoInteractions(userDocumentLastOpenedQueryUseCase);
    }

}
package com.haru.api.meeting.application.service;

import com.haru.api.global.annotation.CreateDocument;
import com.haru.api.global.application.port.FileExtractorService;
import com.haru.api.meeting.application.port.in.MeetingCommandUseCase;
import com.haru.api.meeting.application.port.out.AudioUploadPort;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.meeting.application.converter.MeetingConverter;
import com.haru.api.meeting.presentation.dto.MeetingRequestDTO;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.annotation.DeleteDocument;
import com.haru.api.global.annotation.UpdateDocument;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.*;
import com.haru.api.infra.api.client.ChatGPTClient;
import com.haru.api.infra.api.entity.SpeechSegment;
import com.haru.api.infra.api.repository.SpeechSegmentRepository;
import com.haru.api.infra.s3.MarkdownFileUploader;
import com.haru.api.infra.websocket.AudioSessionBuffer;
import com.haru.api.infra.websocket.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.CloseStatus;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.haru.api.user.domain.enums.DocumentType.AI_MEETING_MANAGER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingCommandUseCaseImpl implements MeetingCommandUseCase {

    private final MeetingPort meetingPort;
    private final AudioUploadPort audioUploadPort;

    private final UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;
    private final UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    private final ChatGPTClient chatGPTClient;
    private final WebSocketSessionRegistry webSocketSessionRegistry;
    private final SpeechSegmentRepository speechSegmentRepository;
    private final MarkdownFileUploader markdownFileUploader;
    private final FileExtractorService fileExtractorService;

    @Override
    @Transactional
    @CreateDocument(documentType = AI_MEETING_MANAGER)
    public MeetingResponseDTO.createMeetingResponse createMeeting(
            User user,
            Workspace workspace,
            MultipartFile agendaFile,
            MeetingRequestDTO.createMeetingRequest request)
    {

        String extractedText = fileExtractorService.extractTextFromFile(agendaFile);

        String agendaSummary = chatGPTClient.summarizeDocument(extractedText)
                .block();

        Meeting newMeeting = Meeting.createInitialMeeting(
                request.getTitle(),
                agendaSummary,
                user,
                workspace
        );

        Meeting savedMeeting = meetingPort.save(newMeeting);

        return MeetingConverter.toCreateMeetingResponse(savedMeeting);
    }

    @Override
    @Transactional
    @UpdateDocument
    public void updateMeetingTitle(User user, Meeting meeting, MeetingRequestDTO.updateTitle request) {

        // 회의 생성자 권한 확인
        if (!meeting.getCreator().getId().equals(user.getId())) {
            throw new MemberHandler(ErrorStatus.MEMBER_NO_AUTHORITY);
        }

        meeting.updateTitle(request.getTitle());

        markdownFileUploader.updateFileTitle(meeting.getProceedingPdfKeyName(), request.getTitle() + ".pdf");
        markdownFileUploader.updateFileTitle(meeting.getProceedingWordKeyName(), request.getTitle() + ".docx");

        meetingPort.save(meeting);
    }

    @Override
    @Transactional
    @DeleteDocument
    public void deleteMeeting(User user, Meeting meeting) {
        Meeting foundMeeting = meetingPort.findById(meeting.getId())
                .orElseThrow(() -> new MeetingHandler(ErrorStatus.MEETING_NOT_FOUND));

        UserWorkspace foundUserWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), foundMeeting.getWorkspace().getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        if (!foundMeeting.getCreator().getId().equals(user.getId()) && !foundUserWorkspace.getAuth().equals(Auth.ADMIN)) {
            throw new MemberHandler(ErrorStatus.MEMBER_NO_AUTHORITY);
        }

        markdownFileUploader.deleteS3File(foundMeeting.getProceedingPdfKeyName());
        markdownFileUploader.deleteS3File(foundMeeting.getThumbnailKeyName());
        markdownFileUploader.deleteS3File(foundMeeting.getProceedingWordKeyName());
        markdownFileUploader.deleteS3File(foundMeeting.getAudioFileKey());


        List<SpeechSegment> segmentsToDelete = speechSegmentRepository.findByMeeting(foundMeeting);

        speechSegmentRepository.deleteAll(segmentsToDelete);

        meetingPort.delete(foundMeeting);
    }

    @Override
    @Transactional
    public void adjustProceeding(User user, Meeting meeting, MeetingRequestDTO.meetingProceedingRequest newProceeding){
        Meeting foundMeeting = meetingPort.findById(meeting.getId())
                .orElseThrow(() -> new MeetingHandler(ErrorStatus.MEETING_NOT_FOUND));

        UserWorkspace foundUserWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), foundMeeting.getWorkspace().getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        if (!foundMeeting.getCreator().getId().equals(user.getId()) && !foundUserWorkspace.getAuth().equals(Auth.ADMIN)) {
            throw new MemberHandler(ErrorStatus.MEMBER_NO_AUTHORITY);
        }
        String editedProceeding = newProceeding.getProceeding();
        foundMeeting.updateProceeding(editedProceeding);
        try {
            // 생성된 PDF를 S3에 업로드
            String pdfKey = markdownFileUploader.createOrUpdatePdf(editedProceeding, "meeting/pdf", meeting.getProceedingPdfKeyName(), meeting.getTitle());
            String wordKey = markdownFileUploader.createOrUpdateWord(editedProceeding, "meeting/word", meeting.getProceedingWordKeyName(), meeting.getTitle());

            // 썸네일 생성 및 업데이트
            String newThumbnailKey = markdownFileUploader.createOrUpdateThumbnail(pdfKey, "meeting" + meeting.getId(), meeting.getThumbnailKeyName());
            log.info("회의록 썸네일 생성/업데이트 완료. Key: {}", newThumbnailKey);
            // Meeting AI 회의록 수정 시 워크스페이스에 속해있는 모든 유저에 대해 썸네일 이미지 키 수정
            List<UserDocumentLastOpened> foundLastOpenedList = userDocumentLastOpenedQueryUseCase.getDocumentAccessHistory(foundMeeting.getId(), AI_MEETING_MANAGER);
            foundLastOpenedList.forEach(userDocumentLastOpened -> {
                userDocumentLastOpened.updateThumbnailKeyName(newThumbnailKey);
            });

        } catch (Exception e) {
            log.error("meetingId: {}의 PDF 또는 썸네일 생성/업로드 중 에러 발생", meeting.getId(), e);
            throw new RuntimeException("파일 갱신 중 오류가 발생했습니다.", e);
        }

    }

    @Override
    @Transactional
    public void endMeeting(User user, Meeting meeting) {

        Long meetingId = meeting.getId();

        // 웹소켓 연결 종료 및 세션 삭제
        try {
            webSocketSessionRegistry.getSession(meetingId).close(CloseStatus.BAD_DATA.withReason("Invalid path"));
            webSocketSessionRegistry.removeSession(meetingId);
        } catch (Exception e) {
            log.error("meetingId: {} session 종료 오류", meetingId);
        }
    }

    @Override
    @Async
    @Transactional
    public void processAfterMeeting(AudioSessionBuffer sessionBuffer) {

        // 현재 처리하고자 하는 session의 meeting entity
        Meeting currentMeeting = meetingPort.findById(sessionBuffer.getMeeting().getId())
                .orElseThrow(() -> new MeetingHandler(ErrorStatus.MEETING_NOT_FOUND));

        // 버퍼에서 오디오 스트림 가져오기
        ByteArrayOutputStream audioBuffer = sessionBuffer.getAllBytes();

        if (audioBuffer != null && audioBuffer.size() > 0) {
            // 파일 업로드 후, key name을 반환
            String keyName = audioUploadPort.uploadAudioFile(audioBuffer);

            // audio file key name 엔티티에 저장
            currentMeeting.setAudioFileKey(keyName);

            // AI 회의록 생성
            List<SpeechSegment> segments = speechSegmentRepository.findByMeeting(currentMeeting);

            if (segments.isEmpty()) {
                log.warn("meetingId: {}에 대한 대화 내용이 없어 AI 요약을 생략합니다.", currentMeeting.getId());
                return;
            }

            // 모든 대화 텍스트를 하나의 문자열로 조합
            String documentText = segments.stream()
                    .map(SpeechSegment::getText)
                    .collect(Collectors.joining("\n"));

            String agendaResult = currentMeeting.getAgendaResult();

            // 동기적 분석 요청
            String analysisResult = chatGPTClient.analyzeMeetingTranscript(documentText, agendaResult).block();

            // 분석 결과 업데이트
            if (analysisResult != null && !analysisResult.isBlank()) {
                currentMeeting.updateProceeding(analysisResult);

                // --- PDF 및 썸네일 생성/업데이트 로직 시작 ---
                try {
                    // 생성된 PDF를 S3에 업로드
                    String pdfKey = markdownFileUploader.createOrUpdatePdf(analysisResult, "meeting/pdf", currentMeeting.getProceedingPdfKeyName(), currentMeeting.getTitle());
                    String wordKey = markdownFileUploader.createOrUpdateWord(analysisResult, "meeting/word", currentMeeting.getProceedingWordKeyName(), currentMeeting.getTitle());
                    currentMeeting.initProceedingPdfKeyName(pdfKey);
                    currentMeeting.initProceedingWordKeyName(wordKey);

                    // 썸네일 생성 및 업데이트
                    String newThumbnailKey = markdownFileUploader.createOrUpdateThumbnail(pdfKey, "meeting" + currentMeeting.getId(), currentMeeting.getThumbnailKeyName());
                    currentMeeting.initThumbnailKeyName(newThumbnailKey); // Meeting 엔티티에 썸네일 키 저장
                    log.info("회의록 썸네일 생성/업데이트 완료. Key: {}", newThumbnailKey);


                    List<UserDocumentLastOpened> foundLastOpenedList = userDocumentLastOpenedQueryUseCase.getDocumentAccessHistory(currentMeeting.getId(), AI_MEETING_MANAGER);
                    foundLastOpenedList.forEach(userDocumentLastOpened -> {
                        userDocumentLastOpened.updateThumbnailKeyName(newThumbnailKey);
                    });



                } catch (Exception e) {
                    log.error("meetingId: {}의 PDF 또는 썸네일 생성/업로드 중 에러 발생", currentMeeting.getId(), e);
                    throw new MeetingHandler(ErrorStatus.MEETING_FILE_UPLOAD_FAIL);
                }
                log.info("meetingId: {}의 AI 회의록 생성 및 저장 완료.", currentMeeting.getId());
            } else {
                log.warn("meetingId: {}의 AI 분석 결과가 비어있습니다.", currentMeeting.getId());
            }

        } else {
            log.warn("meetingId: {}에 처리할 오디오 데이터가 없습니다.", currentMeeting.getId());
        }
    }

    @Override
    public Meeting save(Meeting meeting) {
        return meetingPort.save(meeting);
    }

}

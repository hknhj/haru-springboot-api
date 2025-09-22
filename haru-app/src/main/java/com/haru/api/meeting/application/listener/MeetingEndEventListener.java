package com.haru.api.meeting.application.listener;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MeetingHandler;
import com.haru.api.infra.api.client.ChatGPTClient;
import com.haru.api.infra.api.entity.SpeechSegment;
import com.haru.api.infra.api.repository.SpeechSegmentRepository;
import com.haru.api.infra.s3.MarkdownFileUploader;
import com.haru.api.infra.websocket.AudioSessionBuffer;
import com.haru.api.meeting.application.event.MeetingEndedEvent;
import com.haru.api.meeting.application.port.out.AudioUploadPort;
import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.UserDocumentLastOpened;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static com.haru.api.user.domain.enums.DocumentType.AI_MEETING_MANAGER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEndEventListener {

    private final MeetingPort meetingPort;
    private final AudioUploadPort audioUploadPort;

    private final UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    private final ChatGPTClient chatGPTClient;
    private final SpeechSegmentRepository speechSegmentRepository;
    private final MarkdownFileUploader markdownFileUploader;

    @EventListener
    @Async
    @Transactional
    public void processAfterMeeting(MeetingEndedEvent event) {

        AudioSessionBuffer sessionBuffer = event.getSessionBuffer();

        // 해당되는 Meeting 객체 가져오기
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

            // chatGPT API 사용해서 회의록 분석
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
}

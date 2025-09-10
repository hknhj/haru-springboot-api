package com.haru.api.meeting.application.port.in;

import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingQueryUseCase {

    /**
     * 워크스페이스에 속한 Meeting 리스트를 수정날짜 순서대로 조회하는 메서드
     *
     * @param user
     * @param workspace
     * @return
     */
    List<MeetingResponseDTO.getMeetingResponse> getMeetingList(User user, Workspace workspace);

    /**
     * Meeting 상세 조회 메서드
     *
     * @param user
     * @param meeting
     * @return
     */
    MeetingResponseDTO.getMeetingProceeding getMeetingProceeding(User user, Meeting meeting);

    /**
     * STT 회의 내역 및 AI 추천 질문 조회 메서드
     *
     * @param user
     * @param meeting
     * @return
     */
    MeetingResponseDTO.TranscriptResponse getTranscript(User user, Meeting meeting);

    /**
     * Meeting pdf/docx 문서로 다운로드하는 메서드
     *
     * @param user
     * @param meeting
     * @param format
     * @return
     */
    MeetingResponseDTO.proceedingDownLoadLinkResponse downloadMeeting(User user, Meeting meeting, Format format);

    /**
     * 회의 음성 파일 다운로드 메서드
     *
     * @param user
     * @param meeting
     * @return
     */
    MeetingResponseDTO.proceedingVoiceLinkResponse getMeetingVoiceFile(User user, Meeting meeting);

    /**
     * 워크스페이스에 속한 Meeting 리스트 조회하는 메서드
     *
     * @param workspaceId
     * @return
     */
    List<Meeting> getAllMeetingsInWorkspace(Long workspaceId);

    /**
     * 캘린더에 Meeting을 표시하기 위해 조회하는 메서드
     *
     * @param workspaceId
     * @param startDate
     * @param endDate
     * @return
     */
    List<Meeting> getAllMeetingsForCalendar(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 유저가 해당 워크스페이스에 속하면 해당 Meeting을 조회하는 메서드
     *
     * @param userId
     * @param meetingId
     * @return
     */
    Optional<Meeting> getDocumentWithPermissionCheck(Long userId, Long meetingId);

    /**
     * Meeting 조회 메서드
     *
     * @param meetingId
     * @return
     */
    Meeting getMeeting(Long meetingId);
}

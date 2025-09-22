package com.haru.api.meeting.application.port.in;

import com.haru.api.meeting.presentation.dto.MeetingRequestDTO;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.domain.User;
import com.haru.api.infra.websocket.AudioSessionBuffer;
import com.haru.api.workspace.domain.Workspace;
import org.springframework.web.multipart.MultipartFile;

public interface MeetingCommandUseCase {

    /**
     * Meeting 생성 메서드
     *
     * @param user
     * @param workspace
     * @param agendaFile
     * @param request
     * @return
     */
    MeetingResponseDTO.createMeetingResponse createMeeting(User user, Workspace workspace, MultipartFile agendaFile, MeetingRequestDTO.createMeetingRequest request);

    /**
     * Meeting 제목을 수정하는 메서드
     *
     * @param user
     * @param meeting
     * @param request
     */
    void updateMeetingTitle(User user, Meeting meeting, MeetingRequestDTO.updateTitle request);

    /**
     * Meeting 삭제 메서드
     *
     * @param user
     * @param meeting
     */
    void deleteMeeting(User user, Meeting meeting);

    /**
     * 회의록 정리본 수정 메서드
     *
     * @param user
     * @param meeting
     * @param newProceeding
     */
    void adjustProceeding(User user, Meeting meeting, MeetingRequestDTO.meetingProceedingRequest newProceeding);

    /**
     * Meeting 저장 메서드
     *
     * @param meeting
     * @return
     */
    Meeting save(Meeting meeting);

}

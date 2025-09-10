package com.haru.api.meeting.application.port.in;

import com.haru.api.meeting.presentation.dto.MeetingRequestDTO;
import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.domain.User;
import com.haru.api.infra.websocket.AudioSessionBuffer;
import com.haru.api.workspace.domain.Workspace;
import org.springframework.web.multipart.MultipartFile;

public interface MeetingCommandUseCase {

    MeetingResponseDTO.createMeetingResponse createMeeting(User user, Workspace workspace, MultipartFile agendaFile, MeetingRequestDTO.createMeetingRequest request);

    void updateMeetingTitle(User user, Meeting meeting, MeetingRequestDTO.updateTitle request);

    void deleteMeeting(User user, Meeting meeting);

    void adjustProceeding(User user, Meeting meeting, MeetingRequestDTO.meetingProceedingRequest newProceeding);

    void endMeeting(User user, Meeting meeting);

    void processAfterMeeting(AudioSessionBuffer audioSessionBuffer);

    Meeting save(Meeting meeting);

}

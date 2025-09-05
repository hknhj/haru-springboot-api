package com.haru.api.meeting.application.port.in;

import com.haru.api.meeting.presentation.dto.MeetingResponseDTO;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

import java.util.List;

public interface MeetingQueryUseCase {

    List<MeetingResponseDTO.getMeetingResponse> getMeetings(User user, Workspace workspace);

    MeetingResponseDTO.getMeetingProceeding getMeetingProceeding(User user, Meeting meeting);

    MeetingResponseDTO.TranscriptResponse getTranscript(User user, Meeting meeting);

    MeetingResponseDTO.proceedingDownLoadLinkResponse downloadMeeting(User user, Meeting meeting, Format format);

    MeetingResponseDTO.proceedingVoiceLinkResponse getMeetingVoiceFile(User user, Meeting meeting);
}

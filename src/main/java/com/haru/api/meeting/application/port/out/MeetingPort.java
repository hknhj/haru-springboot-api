package com.haru.api.meeting.application.port.out;

import com.haru.api.meeting.domain.Meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingPort {

    Meeting save(Meeting meeting);

    void delete(Meeting meeting);

    Optional<Meeting> findById(Long meetingId);

    List<Meeting> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId);

    List<Meeting> findAllByWorkspaceId(Long workspaceId);

    List<Meeting> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Meeting> findByIdIfUserHasAccess(Long userId, Long meetingId);

}

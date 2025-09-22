package com.haru.api.meeting.infrastructure.adapter;

import com.haru.api.meeting.application.port.out.MeetingPort;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.infrastructure.jpa.MeetingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MeetingPersistenceAdapter implements MeetingPort {

    private final MeetingJpaRepository meetingJpaRepository;

    @Override
    public Meeting save(Meeting meeting) {
        return meetingJpaRepository.save(meeting);
    }

    @Override
    public void delete(Meeting meeting) {
        meetingJpaRepository.delete(meeting);
    }

    @Override
    public Optional<Meeting> findById(Long meetingId) {
        return meetingJpaRepository.findById(meetingId);
    }

    @Override
    public List<Meeting> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId) {
        return meetingJpaRepository.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
    }

    @Override
    public List<Meeting> findAllByWorkspaceId(Long workspaceId) {
        return meetingJpaRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    public List<Meeting> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate) {
        return meetingJpaRepository.findAllForCalendars(workspaceId, startDate, endDate);
    }

    @Override
    public Optional<Meeting> findByIdIfUserHasAccess(Long userId, Long meetingId) {
        return meetingJpaRepository.findByIdIfUserHasAccess(userId, meetingId);
    }
}

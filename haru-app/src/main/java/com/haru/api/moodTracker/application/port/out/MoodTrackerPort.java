package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.MoodTracker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MoodTrackerPort {

    MoodTracker save(MoodTracker moodTracker);

    void delete(MoodTracker moodTracker);

    Optional<MoodTracker> findById(Long id);

    List<MoodTracker> findAllByWorkspaceId(Long workspaceId);

    List<MoodTracker> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId);

    List<MoodTracker> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<MoodTracker> findMoodTrackerByIdIfUserHasAccess(Long userId, Long moodTrackerId);

    void addRespondentsNum(Long moodTrackerId);

}

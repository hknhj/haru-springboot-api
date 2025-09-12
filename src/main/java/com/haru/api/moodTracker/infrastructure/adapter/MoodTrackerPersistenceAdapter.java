package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.MoodTrackerPort;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.moodTracker.infrastructure.jpa.MoodTrackerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MoodTrackerPersistenceAdapter implements MoodTrackerPort {

    private final MoodTrackerJpaRepository moodTrackerJpaRepository;

    @Override
    public MoodTracker save(MoodTracker moodTracker) {
        return moodTrackerJpaRepository.save(moodTracker);
    }

    @Override
    public void delete(MoodTracker moodTracker) {
        moodTrackerJpaRepository.delete(moodTracker);
    }

    @Override
    public Optional<MoodTracker> findById(Long id) {
        return moodTrackerJpaRepository.findById(id);
    }

    @Override
    public List<MoodTracker> findAllByWorkspaceId(Long workspaceId) {
        return moodTrackerJpaRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    public List<MoodTracker> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId) {
        return moodTrackerJpaRepository.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
    }

    @Override
    public List<MoodTracker> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate) {
        return moodTrackerJpaRepository.findAllForCalendars(workspaceId, startDate, endDate);
    }

    @Override
    public Optional<MoodTracker> findMoodTrackerByIdIfUserHasAccess(Long userId, Long moodTrackerId) {
        return moodTrackerJpaRepository.findMoodTrackerByIdIfUserHasAccess(userId, moodTrackerId);
    }

    @Override
    public void addRespondentsNum(Long moodTrackerId) {
        moodTrackerJpaRepository.addRespondentsNum(moodTrackerId);
    }
}

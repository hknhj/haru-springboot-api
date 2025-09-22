package com.haru.api.moodTracker.infrastructure.jpa;

import com.haru.api.moodTracker.domain.MoodTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodTrackerJpaRepository extends JpaRepository<MoodTracker, Long> {

    @Query("SELECT m FROM MoodTracker m WHERE m.workspace.id = :workspaceId")
    List<MoodTracker> findAllByWorkspaceId(Long workspaceId);

    @Query("SELECT m FROM MoodTracker m WHERE m.workspace.id = :workspaceId ORDER BY m.updatedAt DESC")
    List<MoodTracker> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId);

    @Query("SELECT mt FROM MoodTracker mt " +
            "WHERE mt.workspace.id = :workspaceId " +
            "AND mt.createdAt BETWEEN :startDate AND :endDate")
    List<MoodTracker> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT mt FROM MoodTracker mt " +
            "WHERE mt.id = :moodTrackerId AND EXISTS (" +
            "  SELECT 1 FROM UserWorkspace uw " +
            "  WHERE uw.user.id = :userId AND uw.workspace.id = mt.workspace.id" +
            ")")
    Optional<MoodTracker> findMoodTrackerByIdIfUserHasAccess(Long userId, Long moodTrackerId);

    @Modifying
    @Transactional
    @Query("UPDATE MoodTracker m SET m.respondentsNum = m.respondentsNum + 1 WHERE m.id = :moodTrackerId")
    void addRespondentsNum(Long moodTrackerId);
}

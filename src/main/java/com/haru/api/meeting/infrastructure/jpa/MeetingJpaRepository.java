package com.haru.api.meeting.infrastructure.jpa;

import com.haru.api.meeting.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

    @Query("SELECT m " +
            "FROM Meeting m " +
            "WHERE m.workspace.id = :workspaceId " +
            "ORDER BY m.updatedAt DESC")
    List<Meeting> findAllByWorkspaceIdOrderByUpdatedAtDesc(Long workspaceId);

    @Query("SELECT m " +
            "FROM Meeting m " +
            "WHERE m.workspace.id = :workspaceId")
    List<Meeting> findAllByWorkspaceId(Long workspaceId);

    @Query("SELECT mt FROM Meeting mt " +
            "WHERE mt.workspace.id = :workspaceId " +
            "AND mt.createdAt BETWEEN :startDate AND :endDate")
    List<Meeting> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT m FROM Meeting m " +
            "WHERE m.id = :meetingId AND EXISTS (" +
            "  SELECT 1 FROM UserWorkspace uw " +
            "  WHERE uw.user.id = :userId AND uw.workspace.id = m.workspace.id" +
            ")")
    Optional<Meeting> findByIdIfUserHasAccess(Long userId, Long meetingId);
}

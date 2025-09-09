package com.haru.api.workspace.infrastructure.jpa;

import com.haru.api.workspace.domain.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceInvitationJpaRepository extends JpaRepository<WorkspaceInvitation, Long> {
    Optional<WorkspaceInvitation> findByToken(String token);
}

package com.haru.api.workspace.infrastructure.adapter;

import com.haru.api.workspace.application.port.out.WorkspaceInvitationPort;
import com.haru.api.workspace.domain.WorkspaceInvitation;
import com.haru.api.workspace.infrastructure.jpa.WorkspaceInvitationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkspaceInvitationPersistenceAdapter implements WorkspaceInvitationPort {

    private final WorkspaceInvitationJpaRepository workspaceInvitationJpaRepository;

    @Override
    public WorkspaceInvitation save(WorkspaceInvitation workspaceInvitation) {
        return workspaceInvitationJpaRepository.save(workspaceInvitation);
    }

    @Override
    public Optional<WorkspaceInvitation> findByToken(String token) {
        return workspaceInvitationJpaRepository.findByToken(token);
    }
}

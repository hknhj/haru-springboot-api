package com.haru.api.workspace.application.port.out;

import com.haru.api.workspace.domain.WorkspaceInvitation;

import java.util.Optional;

public interface WorkspaceInvitationPort {

    WorkspaceInvitation save(WorkspaceInvitation workspaceInvitation);

    Optional<WorkspaceInvitation> findByToken(String token);

}

package com.haru.api.workspace.application.port.out;

import com.haru.api.workspace.domain.Workspace;

import java.util.Optional;

public interface WorkspacePort {

    Workspace save(Workspace workspace);

    Optional<Workspace> findById(Long id);

}

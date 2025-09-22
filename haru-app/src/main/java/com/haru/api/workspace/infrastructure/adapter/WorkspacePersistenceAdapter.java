package com.haru.api.workspace.infrastructure.adapter;

import com.haru.api.workspace.application.port.out.WorkspacePort;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.infrastructure.jpa.WorkspaceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkspacePersistenceAdapter implements WorkspacePort {

    private final WorkspaceJpaRepository workspaceJpaRepository;

    @Override
    public Workspace save(Workspace Workspace) {
        return workspaceJpaRepository.save(Workspace);
    }

    @Override
    public Optional<Workspace> findById(Long id) {
        return workspaceJpaRepository.findById(id);
    }
}

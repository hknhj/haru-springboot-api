package com.haru.api.workspace.infrastructure.jpa;

import com.haru.api.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceJpaRepository extends JpaRepository<Workspace, Long> {

}

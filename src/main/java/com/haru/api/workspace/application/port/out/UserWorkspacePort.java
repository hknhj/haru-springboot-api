package com.haru.api.workspace.application.port.out;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;

import java.util.List;
import java.util.Optional;

public interface UserWorkspacePort {

    UserWorkspace save(UserWorkspace userWorkspace);

    Optional<UserWorkspace> findById(Long id);

    List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspacesWithTitle(Long userId);

    List<String> findEmailsByWorkspaceId(Long workspaceId);

    Boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    Optional<UserWorkspace> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    Optional<UserWorkspace> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    Optional<UserWorkspace> findByWorkspaceAndAuth(Workspace workspace, Auth auth);

    List<User> findUsersByWorkspaceId(Long workspaceId);
}

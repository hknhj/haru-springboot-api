package com.haru.api.workspace.infrastructure.adapter;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserWorkspacePersistenceAdapter implements UserWorkspacePort {

    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;

    @Override
    public UserWorkspace save(UserWorkspace userWorkspace) {
        return userWorkspaceJpaRepository.save(userWorkspace);
    }

    @Override
    public Optional<UserWorkspace> findById(Long id) {
        return userWorkspaceJpaRepository.findById(id);
    }

    @Override
    public List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspacesWithTitle(Long userId) {
        return userWorkspaceJpaRepository.getUserWorkspacesWithTitle(userId);
    }

    @Override
    public List<String> findEmailsByWorkspaceId(Long workspaceId) {
        return userWorkspaceJpaRepository.findEmailsByWorkspaceId(workspaceId);
    }

    @Override
    public Boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId) {
        return userWorkspaceJpaRepository.existsByUserIdAndWorkspaceId(userId, workspaceId);
    }

    @Override
    public Optional<UserWorkspace> findByWorkspaceIdAndUserId(Long workspaceId, Long userId) {
        return userWorkspaceJpaRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    public Optional<UserWorkspace> findByUserIdAndWorkspaceId(Long userId, Long workspaceId) {
        return userWorkspaceJpaRepository.findByUserIdAndWorkspaceId(userId, workspaceId);
    }

    @Override
    public Optional<UserWorkspace> findByWorkspaceAndAuth(Workspace workspace, Auth auth) {
        return userWorkspaceJpaRepository.findByWorkspaceAndAuth(workspace, auth);
    }

    @Override
    public List<User> findUsersByWorkspaceId(Long workspaceId) {
        return userWorkspaceJpaRepository.findUsersByWorkspaceId(workspaceId);
    }
}

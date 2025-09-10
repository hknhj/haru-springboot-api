package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;

import java.util.List;
import java.util.Optional;

public interface UserWorkspaceQueryUseCase {

    List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspaceList(User user);

    Boolean isUserMemberOfWorkspace(Long userId, Long workspaceId);

    List<User> getWorkspaceMembers(Long workspaceId);

    Optional<UserWorkspace> getUserWorkspace(Long userId, Long workspaceId);
}

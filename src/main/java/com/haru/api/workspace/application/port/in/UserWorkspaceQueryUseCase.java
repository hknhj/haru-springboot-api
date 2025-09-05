package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;

import java.util.List;

public interface UserWorkspaceQueryUseCase {

    List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspaceList(User user);
}

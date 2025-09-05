package com.haru.api.domain.userWorkspace.service;

import com.haru.api.user.domain.User;
import com.haru.api.domain.userWorkspace.dto.UserWorkspaceResponseDTO;

import java.util.List;

public interface UserWorkspaceQueryService {

    List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspaceList(User user);
}

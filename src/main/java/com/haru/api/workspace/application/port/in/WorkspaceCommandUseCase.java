package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.presentation.dto.WorkspaceRequestDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;
import org.springframework.web.multipart.MultipartFile;

public interface WorkspaceCommandUseCase {

    WorkspaceResponseDTO.Workspace createWorkspace(User user, WorkspaceRequestDTO.WorkspaceCreateRequest request, MultipartFile image);

    WorkspaceResponseDTO.Workspace updateWorkspace(User user, Workspace workspace, WorkspaceRequestDTO.WorkspaceUpdateRequest request, MultipartFile image);

    WorkspaceResponseDTO.InvitationAcceptResult acceptInvite(String token);

    WorkspaceResponseDTO.InvitationAcceptResult acceptInvite(String token, User user);

    void sendInviteEmail(User user, WorkspaceRequestDTO.WorkspaceInviteEmailRequest request);
}

package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.presentation.dto.WorkspaceRequestDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;
import org.springframework.web.multipart.MultipartFile;

public interface WorkspaceCommandUseCase {

    /**
     * workspace 생성 메서드
     *
     * @param user
     * @param request
     * @param image
     * @return
     */
    WorkspaceResponseDTO.Workspace createWorkspace(User user, WorkspaceRequestDTO.WorkspaceCreateRequest request, MultipartFile image);

    /**
     * workspace 제목 수정 메서드
     *
     * @param user
     * @param workspace
     * @param request
     * @param image
     * @return
     */
    WorkspaceResponseDTO.Workspace updateWorkspace(User user, Workspace workspace, WorkspaceRequestDTO.WorkspaceUpdateRequest request, MultipartFile image);

    /**
     * 워크스페이스 초대 메일 클릭 시 호출되는 초대 수락 메서드
     *
     * @param token
     * @return
     */
    WorkspaceResponseDTO.InvitationAcceptResult acceptInvite(String token);

    /**
     * 회원가입 시 토큰이 있는 경우 호출되는 초대 수락 메서드
     *
     * @param token
     * @param user
     */
    void acceptInvite(String token, User user);

    /**
     * 워크스페이스 초대 메일을 보내는 메서드
     *
     * @param user
     * @param request
     */
    void sendInviteEmail(User user, WorkspaceRequestDTO.WorkspaceInviteEmailRequest request);
}

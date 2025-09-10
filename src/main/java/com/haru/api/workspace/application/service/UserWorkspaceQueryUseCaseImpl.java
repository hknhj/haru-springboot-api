package com.haru.api.workspace.application.service;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;
import com.haru.api.infra.s3.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserWorkspaceQueryUseCaseImpl implements UserWorkspaceQueryUseCase {

    private final UserWorkspacePort userWorkspacePort;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    public List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspaceList(User user) {

        List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> workspaceList =
                userWorkspacePort.getUserWorkspacesWithTitle(user.getId());

        workspaceList.forEach(workspace -> {
            String presignedUrl = amazonS3Manager.generatePresignedUrl(workspace.getImageUrl());
            workspace.setImageUrl(presignedUrl);
        });

        return workspaceList;
    }

    @Override
    public Boolean isUserMemberOfWorkspace(Long userId, Long workspaceId) {
        return userWorkspacePort.existsByUserIdAndWorkspaceId(userId, workspaceId);
    }

    @Override
    public List<User> getWorkspaceMembers(Long workspaceId) {
        return userWorkspacePort.findUsersByWorkspaceId(workspaceId);
    }

    @Override
    public Optional<UserWorkspace> getUserWorkspace(Long userId, Long workspaceId) {
        return userWorkspacePort.findByUserIdAndWorkspaceId(userId, workspaceId);
    }
}

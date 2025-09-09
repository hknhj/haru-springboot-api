package com.haru.api.workspace.application.service;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.presentation.dto.UserWorkspaceResponseDTO;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.infra.s3.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWorkspaceQueryUseCaseImpl implements UserWorkspaceQueryUseCase {

    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    public List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> getUserWorkspaceList(User user) {

        List<UserWorkspaceResponseDTO.UserWorkspaceWithTitle> workspaceList =
                userWorkspaceJpaRepository.getUserWorkspacesWithTitle(user.getId());

        workspaceList.forEach(workspace -> {
            String presignedUrl = amazonS3Manager.generatePresignedUrl(workspace.getImageUrl());
            workspace.setImageUrl(presignedUrl);
        });

        return workspaceList;
    }
}

package com.haru.api.user.application.service;

import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.application.port.out.UserDocumentLastOpenedPort;
import com.haru.api.user.domain.UserDocumentLastOpened;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDocumentLastOpenedQueryUseCaseImpl implements UserDocumentLastOpenedQueryUseCase {

    private final UserDocumentLastOpenedPort userDocumentLastOpenedPort;

    @Override
    public List<UserDocumentLastOpened> getRecentDocumentsByTitle(Long userId, Long workspaceId, String title) {
        return userDocumentLastOpenedPort.findRecentDocumentsByTitle(userId, workspaceId, title);
    }

    @Override
    public List<UserDocumentLastOpened> getRecentDocuments(Long userId, Long workspaceId, PageRequest pageRequest) {
        return userDocumentLastOpenedPort.findRecentDocuments(userId, workspaceId, pageRequest);
    }
}

package com.haru.api.workspace.infrastructure.adapter;

import com.haru.api.workspace.application.port.out.UserDocumentLastOpenedPort;
import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
import com.haru.api.workspace.domain.enums.DocumentType;
import com.haru.api.workspace.infrastructure.jpa.UserDocumentLastOpenedJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDocumentLastOpenedPersistenceAdapter implements UserDocumentLastOpenedPort {

    private final UserDocumentLastOpenedJpaRepository userDocumentLastOpenedJpaRepository;

    @Override
    public UserDocumentLastOpened save(UserDocumentLastOpened userDocumentLastOpened) {
        return userDocumentLastOpenedJpaRepository.save(userDocumentLastOpened);
    }

    @Override
    public Optional<UserDocumentLastOpened> findById(UserDocumentId id) {
        return userDocumentLastOpenedJpaRepository.findById(id);
    }

    @Override
    public List<UserDocumentLastOpened> findRecentDocuments(Long userId, Long workspaceId, Pageable pageable) {
        return userDocumentLastOpenedJpaRepository.findRecentDocuments(workspaceId, userId, pageable);
    }

    @Override
    public List<UserDocumentLastOpened> findRecentDocumentsByTitle(Long userId, Long workspaceId, String title) {
        return userDocumentLastOpenedJpaRepository.findRecentDocumentsByTitle(workspaceId, userId, title);
    }

    @Override
    public List<UserDocumentLastOpened> findByDocumentIdAndDocumentType(Long documentId, DocumentType documentType) {
        return userDocumentLastOpenedJpaRepository.findByDocumentIdAndDocumentType(documentId, documentType);
    }
}

package com.haru.api.workspace.application.port.out;

import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
import com.haru.api.workspace.domain.enums.DocumentType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserDocumentLastOpenedPort {

    UserDocumentLastOpened save(UserDocumentLastOpened userDocumentLastOpened);

    Optional<UserDocumentLastOpened> findById(UserDocumentId id);

    List<UserDocumentLastOpened> findRecentDocuments(Long userId, Long workspaceId, Pageable pageable);

    List<UserDocumentLastOpened> findRecentDocumentsByTitle(Long userId, Long workspaceId, String title);

    List<UserDocumentLastOpened> findByDocumentIdAndDocumentType(Long documentId, DocumentType documentType);
}

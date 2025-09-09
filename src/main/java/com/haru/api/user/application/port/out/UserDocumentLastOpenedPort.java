package com.haru.api.user.application.port.out;

import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.enums.DocumentType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserDocumentLastOpenedPort {

    UserDocumentLastOpened save(UserDocumentLastOpened userDocumentLastOpened);

    void saveAll(List<UserDocumentLastOpened> recordsToSave);

    void deleteAll(List<UserDocumentLastOpened> recordsToDelete);

    Optional<UserDocumentLastOpened> findById(UserDocumentId id);

    List<UserDocumentLastOpened> findRecentDocuments(Long userId, Long workspaceId, Pageable pageable);

    List<UserDocumentLastOpened> findRecentDocumentsByTitle(Long userId, Long workspaceId, String title);

    List<UserDocumentLastOpened> findByDocumentIdAndDocumentType(Long documentId, DocumentType documentType);
}

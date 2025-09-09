package com.haru.api.workspace.infrastructure.jpa;

import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
import com.haru.api.workspace.domain.enums.DocumentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDocumentLastOpenedJpaRepository extends JpaRepository<UserDocumentLastOpened, Long> {

    Optional<UserDocumentLastOpened> findById(UserDocumentId id);

    @Query("SELECT udlo " +
            "FROM UserDocumentLastOpened udlo " +
            "WHERE udlo.user.id = :userId " +
            "AND udlo.workspaceId = :workspaceId " +
            "ORDER BY udlo.lastOpened DESC NULLS LAST, udlo.id.documentId DESC")
    List<UserDocumentLastOpened> findRecentDocuments(Long userId, Long workspaceId, Pageable pageable);

    @Query("SELECT udlo " +
            "FROM UserDocumentLastOpened udlo " +
            "WHERE udlo.workspaceId = :workspaceId AND udlo.id.userId = :userId " +
            "AND udlo.title LIKE %:title% " +
            "ORDER BY udlo.lastOpened DESC")
    List<UserDocumentLastOpened> findRecentDocumentsByTitle(Long userId, Long workspaceId, String title);

    @Query("SELECT udlo " +
            "FROM UserDocumentLastOpened udlo " +
            "WHERE udlo.id.documentId = :documentId " +
            "AND udlo.id.documentType = :documentType")
    List<UserDocumentLastOpened> findByDocumentIdAndDocumentType(Long documentId, DocumentType documentType);
}

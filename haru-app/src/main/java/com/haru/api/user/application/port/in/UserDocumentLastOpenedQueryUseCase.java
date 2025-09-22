package com.haru.api.user.application.port.in;

import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.enums.DocumentType;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserDocumentLastOpenedQueryUseCase {

    List<UserDocumentLastOpened> getRecentDocumentsByTitle(Long userId, Long workspaceId, String title);

    List<UserDocumentLastOpened> getRecentDocuments(Long userId, Long workspaceId, PageRequest pageRequest);

    List<UserDocumentLastOpened> getDocumentAccessHistory(Long documentId, DocumentType documentType);
}

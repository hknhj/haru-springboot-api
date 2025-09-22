package com.haru.api.common_library.domain;

import com.haru.api.user.domain.enums.DocumentType;

import java.time.LocalDateTime;

public interface Documentable {
    Long getId();
    String getTitle();
    Long getWorkspaceId();
    DocumentType getDocumentType();
    String getThumbnailKeyName();
    LocalDateTime getCreatedAt();
}
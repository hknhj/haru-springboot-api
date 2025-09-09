package com.haru.api.shared_kernel.domain;

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
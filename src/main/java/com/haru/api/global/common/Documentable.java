package com.haru.api.global.common;

import com.haru.api.user.domain.enums.DocumentType;

public interface Documentable {
    Long getId();
    String getTitle();
    Long getWorkspaceId();
    DocumentType getDocumentType();
    String getThumbnailKeyName();
}
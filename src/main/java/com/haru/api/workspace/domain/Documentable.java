package com.haru.api.workspace.domain;

import com.haru.api.workspace.domain.enums.DocumentType;

public interface Documentable {
    Long getId();
    String getTitle();
    Long getWorkspaceId();
    DocumentType getDocumentType();
    String getThumbnailKeyName();
}
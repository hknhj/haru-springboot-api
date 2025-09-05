package com.haru.api.global.documentFinder;

import com.haru.api.workspace.domain.enums.DocumentType;

public interface DocumentFinder {
    DocumentType getSupportType();
    Object findById(Object id);
}

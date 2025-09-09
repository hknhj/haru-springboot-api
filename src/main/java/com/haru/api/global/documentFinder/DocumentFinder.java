package com.haru.api.global.documentFinder;

import com.haru.api.user.domain.enums.DocumentType;

public interface DocumentFinder {
    DocumentType getSupportType();
    Object findById(Object id);
}

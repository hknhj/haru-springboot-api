package com.haru.api.user.application.converter;

import com.haru.api.global.common.Documentable;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.User;

public class UserDocumentLastOpenedConverter {
    public static UserDocumentLastOpened toUserDocumentLastOpened(Documentable document, User user) {

        UserDocumentId userDocumentId = new UserDocumentId(user.getId(), document.getId(), document.getDocumentType());

        return UserDocumentLastOpened.builder()
                .id(userDocumentId)
                .user(user)
                .workspaceId(document.getWorkspaceId())
                .lastOpened(null)
                .thumbnailKeyName(document.getThumbnailKeyName())
                .title(document.getTitle())
                .build();
    }
}

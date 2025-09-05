package com.haru.api.workspace.application.converter;

import com.haru.api.workspace.domain.Documentable;
import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
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

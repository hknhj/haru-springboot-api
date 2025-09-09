package com.haru.api.workspace.application.port.in;

import com.haru.api.workspace.domain.Documentable;
import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.user.domain.User;
import com.haru.api.global.common.entity.TitleHolder;

import java.util.List;

public interface UserDocumentLastOpenedQueryUseCase {

    void updateLastOpened(UserDocumentId userDocumentId, Long workspaceId, String title);

    void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, Documentable document);

    void deleteRecordsForWorkspaceUsers(Documentable document);

    void updateRecordsForWorkspaceUsers(Documentable document, TitleHolder titleHolder);

    void updateRecordsTitleAndThumbnailForWorkspaceUsers(List<User> usersInWorkspace, Documentable documentable, TitleHolder titleHolder);

}

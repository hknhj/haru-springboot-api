package com.haru.api.user.application.port.in;

import com.haru.api.global.common.Documentable;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.User;
import com.haru.api.global.common.entity.DocumentModifier;

import java.util.List;

public interface UserDocumentLastOpenedQueryUseCase {

    /**
     * 문서 생성 시, 워크스페이스에 속해있는 유저의 해당 문서에 대한 UserDocumentLastOpened를 생성하는 메서드
     *
     * @param usersInWorkspace
     * @param document
     */
    void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, Documentable document);

    /**
     * 유저가 특정 문서를 조회 시, 해당 문서의 UserDocumentLastOpened를 업데이트하는 메서드
     * 만약, 특정 문서의 UserDocumentLastOpened가 존재하지 않을 경우, UserDocumentLastOpened 생성 후, last opened 업데이트
     *
     * @param userDocumentId
     * @param workspaceId
     * @param title
     */
    void updateLastOpened(UserDocumentId userDocumentId, Long workspaceId, String title);

    /**
     * 문서 수정 시, 워크스페이스에 속해있는 유저의 해당 문서에 대한 UserDocumentLastOpened를 수정하는 메서드
     *
     * @param document
     * @param documentModifier
     */
    void updateRecordsTitleAndThumbnailForWorkspaceUsers(Documentable document, DocumentModifier documentModifier);

    /**
     * 문서 삭제 시, 워크스페이스에 속해있는 유저의 해당 문서에 대한 UserDocumentLastOpened를 삭제하는 메서드
     *
     * @param document
     */
    void deleteRecordsForWorkspaceUsers(Documentable document);

}

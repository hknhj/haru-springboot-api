package com.haru.api.user.application.port.in;

import com.haru.api.common_library.domain.CreatedDocument;
import com.haru.api.common_library.domain.Documentable;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.User;
import com.haru.api.common_library.domain.DocumentModifier;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.enums.DocumentType;

import java.util.List;

public interface UserDocumentLastOpenedCommandUseCase {

    /**
     * 문서 생성 시, 워크스페이스에 속해있는 유저의 해당 문서에 대한 UserDocumentLastOpened를 생성하는 메서드
     *
     * @param usersInWorkspace
     * @param document
     */
    void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, Documentable document);

    void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, CreatedDocument createdDocument, Long workspaceId, DocumentType documentType);

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

    /**
     * UserDocumentLastOpened를 한 번에 저장하는 메서드
     *
     * @param userDocumentLastOpenedList
     */
    void saveAll(List<UserDocumentLastOpened> userDocumentLastOpenedList);

}

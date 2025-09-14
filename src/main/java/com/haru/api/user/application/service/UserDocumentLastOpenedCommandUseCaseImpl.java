package com.haru.api.user.application.service;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.common_library.domain.CreatedDocument;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.user.application.port.out.UserDocumentLastOpenedPort;
import com.haru.api.common_library.domain.Documentable;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.User;
import com.haru.api.common_library.domain.DocumentModifier;
import com.haru.api.user.domain.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDocumentLastOpenedCommandUseCaseImpl implements UserDocumentLastOpenedCommandUseCase {

    private final UserDocumentLastOpenedPort userDocumentLastOpenedPort;
    private final UserPort userPort;

    @Override
    @Transactional
    public void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, Documentable document) {

        List<UserDocumentLastOpened> recordsToSave = new ArrayList<>();

        for (User user : usersInWorkspace) {
            UserDocumentId documentId = new UserDocumentId(
                    user.getId(),
                    document.getId(),
                    document.getDocumentType()
            );

            UserDocumentLastOpened newRecord = UserDocumentLastOpened.builder()
                    .id(documentId)
                    .user(user)
                    .title(document.getTitle())
                    .workspaceId(document.getWorkspaceId())
                    .thumbnailKeyName(document.getThumbnailKeyName())
                    .build();

            recordsToSave.add(newRecord);
        }

        if (!recordsToSave.isEmpty()) {
            userDocumentLastOpenedPort.saveAll(recordsToSave);
        }
    }

    @Override
    public void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, CreatedDocument createdDocument, Long workspaceId, DocumentType documentType) {

        List<UserDocumentLastOpened> recordsToSave = new ArrayList<>();

        for (User user : usersInWorkspace) {
            UserDocumentId documentId = new UserDocumentId(
                    user.getId(),
                    createdDocument.getId(),
                    documentType
            );

            UserDocumentLastOpened newRecord = UserDocumentLastOpened.builder()
                    .id(documentId)
                    .user(user)
                    .title(createdDocument.getTitle())
                    .workspaceId(workspaceId)
                    .build();

            recordsToSave.add(newRecord);
        }

        if (!recordsToSave.isEmpty()) {
            userDocumentLastOpenedPort.saveAll(recordsToSave);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastOpened(UserDocumentId userDocumentId, Long workspaceId, String title) {

        UserDocumentLastOpened record = userDocumentLastOpenedPort.findById(userDocumentId)
                .orElseGet(() -> {
                    User foundUser = userPort.findById(userDocumentId.getUserId())
                            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
                    return UserDocumentLastOpened.builder()
                            .id(userDocumentId)
                            .user(foundUser)
                            .workspaceId(workspaceId)
                            .title(title)
                            .build();
                });

        record.updateLastOpened(LocalDateTime.now());
        userDocumentLastOpenedPort.save(record);
    }

    @Override
    @Transactional
    public void updateRecordsTitleAndThumbnailForWorkspaceUsers(Documentable documentable, DocumentModifier documentModifier) {

        // 해당 문서 id, 문서 타입에 해당하는 last opened 튜플 검색
        List<UserDocumentLastOpened> recordsToUpdate = userDocumentLastOpenedPort.findByDocumentIdAndDocumentType(documentable.getId(), documentable.getDocumentType());

        if (recordsToUpdate.isEmpty()) return;

        for (UserDocumentLastOpened record : recordsToUpdate) {
            record.updateDetails(documentModifier);
        }

        userDocumentLastOpenedPort.saveAll(recordsToUpdate);
    }

    @Override
    public void deleteRecordsForWorkspaceUsers(Documentable documentable) {

        // 해당 문서 id, 문서 타입에 해당하는 last opened 튜플 검색
        List<UserDocumentLastOpened> recordsToUpdate = userDocumentLastOpenedPort.findByDocumentIdAndDocumentType(documentable.getId(), documentable.getDocumentType());

        if (!recordsToUpdate.isEmpty()) {
            userDocumentLastOpenedPort.deleteAll(recordsToUpdate);
        }

    }

    @Override
    public void saveAll(List<UserDocumentLastOpened> userDocumentLastOpenedList) {
        userDocumentLastOpenedPort.saveAll(userDocumentLastOpenedList);
    }

}

package com.haru.api.workspace.application.service;

import com.haru.api.user.application.port.in.UserQueryUseCase;
import com.haru.api.workspace.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.workspace.application.port.out.UserDocumentLastOpenedPort;
import com.haru.api.workspace.domain.Documentable;
import com.haru.api.workspace.domain.UserDocumentId;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
import com.haru.api.user.domain.User;
import com.haru.api.global.common.entity.TitleHolder;
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
public class UserDocumentLastOpenedQueryUseCaseImpl implements UserDocumentLastOpenedQueryUseCase {

    private final UserDocumentLastOpenedPort userDocumentLastOpenedPort;
    private final UserQueryUseCase userQueryUseCase;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastOpened(UserDocumentId userDocumentId, Long workspaceId, String title) {

        UserDocumentLastOpened record = userDocumentLastOpenedPort.findById(userDocumentId)
                .orElseGet(() -> {
                    User foundUser = userQueryUseCase.findUserById(userDocumentId.getUserId());
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
    public void createInitialRecordsForWorkspaceUsers(List<User> usersInWorkspace, Documentable document) {

        // 저장할 엔티티 리스트 생성
        List<UserDocumentLastOpened> recordsToSave = recordsToProcess(usersInWorkspace, document);

        // 전체 save
        if (!recordsToSave.isEmpty()) {
            userDocumentLastOpenedPort.saveAll(recordsToSave);
        }
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
    @Transactional
    public void updateRecordsForWorkspaceUsers(Documentable documentable, TitleHolder titleHolder) {

        // 해당 문서 id, 문서 타입에 해당하는 last opened 튜플 검색
        List<UserDocumentLastOpened> recordsToUpdate = userDocumentLastOpenedPort.findByDocumentIdAndDocumentType(documentable.getId(), documentable.getDocumentType());

        if (!recordsToUpdate.isEmpty()) {
            for (UserDocumentLastOpened record : recordsToUpdate) {
                record.updateTitle(titleHolder.getTitle());
            }
        }

        userDocumentLastOpenedPort.saveAll(recordsToUpdate);
    }

    @Override
    @Transactional
    public void updateRecordsTitleAndThumbnailForWorkspaceUsers(List<User> usersInWorkspace, Documentable documentable, TitleHolder titleHolder) {
        // 해당 문서 id, 문서 타입에 해당하는 last opened 튜플 검색
        List<UserDocumentLastOpened> recordsToUpdate = userDocumentLastOpenedPort.findByDocumentIdAndDocumentType(documentable.getId(), documentable.getDocumentType());

        if (!recordsToUpdate.isEmpty()) {
            for (UserDocumentLastOpened record : recordsToUpdate) {
                record.updateTitle(titleHolder.getTitle());
                record.updateThumbnailKeyName(documentable.getThumbnailKeyName());
            }
        }
    }

    private List<UserDocumentLastOpened> recordsToProcess(List<User> usersInWorkspace, Documentable document) {
        // 처리할 엔티티들을 담을 리스트 생성
        List<UserDocumentLastOpened> recordsToProcess = new ArrayList<>();

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

            recordsToProcess.add(newRecord);
        }

        return recordsToProcess;
    }

}

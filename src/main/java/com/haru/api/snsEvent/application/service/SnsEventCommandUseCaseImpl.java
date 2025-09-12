package com.haru.api.snsEvent.application.service;

import com.haru.api.global.annotation.CreateDocument;
import com.haru.api.snsEvent.application.port.in.UploadFileAndThumbnailUseCase;
import com.haru.api.snsEvent.application.port.in.WinnerDrawUseCase;
import com.haru.api.snsEvent.application.port.out.*;
import com.haru.api.snsEvent.application.port.in.SnsEventCommandUseCase;
import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.snsEvent.domain.enums.ListType;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.annotation.DeleteDocument;
import com.haru.api.global.annotation.UpdateDocument;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsEventCommandUseCaseImpl implements SnsEventCommandUseCase {

    private final SnsEventPort snsEventPort;
    private final FilePort filePort;

    private final WinnerDrawUseCase winnerDrawUseCase;
    private final UploadFileAndThumbnailUseCase uploadFileAndThumbnailUseCase;
    private final WorkspaceQueryUseCase workspaceQueryUseCase;
    private final UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;

    @Override
    @Transactional
    @CreateDocument(documentType = DocumentType.SNS_EVENT_ASSISTANT)
    public SnsEventResponseDTO.CreateSnsEventResponse createSnsEvent(
            User user,
            Workspace workspace,
            SnsEventRequestDTO.CreateSnsRequest request
    ) {

        Workspace foundWorkspace = workspaceQueryUseCase.getWorkspace(workspace.getId());

        // SNS 이벤트 생성
        SnsEvent createdSnsEvent = SnsEventConverter.toSnsEvent(request, user);
        createdSnsEvent.setWorkspace(foundWorkspace);

        // 참여자, 당첨자 리스트 생성 및 저장
        winnerDrawUseCase.getAndSaveParticipantAndWinner(
                createdSnsEvent,
                foundWorkspace.getInstagramAccessToken(),
                request.getSnsEventLink(),
                request.getSnsCondition()
        );

        // PDF, DOCX파일 바이트 배열로 생성 및 썸네일 생성 & 업로드 / DB에 keyName저장
        String thumbnailKeyName = uploadFileAndThumbnailUseCase.createAndUploadListFileAndThumbnail(createdSnsEvent);

        // sns event 썸네일 key name 초기화
        createdSnsEvent.initThumbnailKeyName(thumbnailKeyName);

        SnsEvent savedSnsEvent = snsEventPort.save(createdSnsEvent);

        return SnsEventResponseDTO.CreateSnsEventResponse.builder()
                .snsEventId(savedSnsEvent.getId())
                .build();
    }

  
    @Override
    @Transactional
    @UpdateDocument
    public void updateSnsEvent(
            User user,
            SnsEvent snsEvent,
            SnsEventRequestDTO.UpdateSnsEventRequest request
    ) {

        SnsEvent foundSnsEvent = snsEventPort.findById(snsEvent.getId());

        UserWorkspace foundUserWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), foundSnsEvent.getWorkspaceId())
                .orElseThrow(() -> new MemberHandler(WORKSPACE_CREATOR_NOT_FOUND));

        // 수정 권한 확인 (워크스페이스 생성자 혹은 SNS 이벤트의 생성자만 수정 가능)
        if (!foundUserWorkspace.getUser().getId().equals(user.getId()) || !foundSnsEvent.getCreator().getId().equals(user.getId())) {
            throw new SnsEventHandler(SNS_EVENT_NO_AUTHORITY);
        }

        foundSnsEvent.updateTitle(request.getTitle());

        // S3문서 제목, S3 문서내 제목, 썸네일 이미지의 제목 변경
        filePort.deleteSnsEventFileAndThumbnailImage(foundSnsEvent);

        String thumbnailKeyName = uploadFileAndThumbnailUseCase.createAndUploadListFileAndThumbnail(foundSnsEvent);
        // sns event 썸네일 key name 초기화
        foundSnsEvent.initThumbnailKeyName(thumbnailKeyName);

        snsEventPort.save(foundSnsEvent);
    }

    @Override
    @Transactional
    @DeleteDocument
    public void deleteSnsEvent(
            User user,
            SnsEvent snsEvent
    ) {

        SnsEvent foundSnsEvent = snsEventPort.findById(snsEvent.getId());

        UserWorkspace foundUserWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(user.getId(), foundSnsEvent.getWorkspaceId())
                .orElseThrow(() -> new MemberHandler(WORKSPACE_CREATOR_NOT_FOUND));

        // 삭제 권한 확인 (워크스페이스 생성자 혹은 SNS 이벤트의 생성자만 삭제 가능)
        if (!foundUserWorkspace.getUser().getId().equals(user.getId()) || !foundSnsEvent.getCreator().getId().equals(user.getId())) {
            throw new SnsEventHandler(SNS_EVENT_NO_AUTHORITY);
        }

        // S3의 문서 및 썸네일 이미지 삭제
        filePort.deleteSnsEventFileAndThumbnailImage(foundSnsEvent);

        snsEventPort.delete(foundSnsEvent);
    }

    @Override
    public SnsEventResponseDTO.ListDownLoadLinkResponse downloadList(
            User user,
            SnsEvent snsEvent,
            ListType listType,
            Format format
    ) {
        String downloadLink = "";

        String snsEventTitle = snsEvent.getTitle();
        if (listType == ListType.PARTICIPANT) {
            if (format == Format.PDF) {
                String keyName = snsEvent.getKeyNameParticipantPdf();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = filePort.getDownloadLink(keyName, snsEventTitle + "_참여자_리스트.pdf");
            } else if (format == Format.DOCX) {
                String keyName = snsEvent.getKeyNameParticipantWord();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = filePort.getDownloadLink(keyName, snsEventTitle + "_참여자_리스트.docx");
            } else {
                throw new SnsEventHandler(SNS_EVENT_WRONG_FORMAT);
            }
        } else if (listType == ListType.WINNER) {
            if (format == Format.PDF) {
                String keyName = snsEvent.getKeyNameWinnerPdf();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = filePort.getDownloadLink(keyName, snsEventTitle + "_당첨자_리스트.pdf");
            } else if (format == Format.DOCX) {
                String keyName = snsEvent.getKeyNameWinnerWord();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = filePort.getDownloadLink(keyName, snsEventTitle + "_당첨자_리스트.docx");
            } else {
                throw new SnsEventHandler(SNS_EVENT_WRONG_FORMAT);
            }
        } else {
            throw new SnsEventHandler(SNS_EVENT_WRONG_LIST_TYPE);
        }
        return SnsEventResponseDTO.ListDownLoadLinkResponse.builder()
                .downloadLink(downloadLink)
                .build();
    }
}

package com.haru.api.workspace.application.service;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceHandler;
import com.haru.api.shared_kernel.application.port.in.DocumentQueryUseCase;
import com.haru.api.shared_kernel.domain.Documentable;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.out.UserWorkspacePort;
import com.haru.api.workspace.application.converter.WorkspaceConverter;
import com.haru.api.workspace.application.port.out.WorkspacePort;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.infra.s3.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceQueryUseCaseImpl implements WorkspaceQueryUseCase {

    private final UserWorkspacePort userWorkspacePort;
    private final WorkspacePort workspacePort;

    private final DocumentQueryUseCase documentQueryUseCase;
    private final UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    private final AmazonS3Manager amazonS3Manager;

    @Override
    public WorkspaceResponseDTO.DocumentList getDocumentsByTitle(User user, Workspace workspace, String title) {

        List<UserDocumentLastOpened> documentList = userDocumentLastOpenedQueryUseCase.getRecentDocumentsByTitle(user.getId(), workspace.getId(), title);

        return WorkspaceConverter.toDocumentList(
                documentList.stream()
                        .map(WorkspaceConverter::toDocument)
                        .toList()
        );
    }

    @Override
    public WorkspaceResponseDTO.DocumentSidebarList getDocumentsForSidebar(User user, Workspace workspace) {

        // 유저가 가장 최근에 조회한 문서 5개 추출
        List<UserDocumentLastOpened> documentList = userDocumentLastOpenedQueryUseCase.getRecentDocuments(user.getId(), workspace.getId(), PageRequest.of(0,5));

        return WorkspaceConverter.toDocumentSidebarList(
                documentList.stream()
                        .map(WorkspaceConverter::toDocumentSidebar)
                        .toList()
        );
    }

    @Override
    public WorkspaceResponseDTO.DocumentCalendarList getDocumentForCalendar(User user, Workspace workspace, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 워크스페이스에 속하면서 생성 날짜가 startDate, endDate 사이인 문서 리스트 검색
        List<Documentable> documentList = documentQueryUseCase.getAllDocumentsForCalendars(workspace.getId(), startDateTime, endDateTime);

        // 모든 문서 합치기
        return WorkspaceConverter.toDocumentCalendarList(
                documentList.stream()
                        .map(WorkspaceConverter::toDocumentCalender)
                        .toList());
    }

    @Override
    public WorkspaceResponseDTO.WorkspaceEditPage getEditPage(User user, Workspace workspace) {

        List<UserResponseDTO.MemberInfo> memberInfoList = userWorkspacePort.findUsersByWorkspaceId(workspace.getId()).stream()
                .map(UserConverter::toMemberInfo)
                .toList();

        String imageUrl = amazonS3Manager.generatePresignedUrl(workspace.getKeyName());

        return WorkspaceConverter.toWorkspaceEditPage(workspace, memberInfoList, imageUrl);
    }

    @Override
    public WorkspaceResponseDTO.RecentDocumentList getDocumentsForMainPage(User user, Workspace workspace) {

        List<UserDocumentLastOpened> recentDocumentList = userDocumentLastOpenedQueryUseCase.getRecentDocuments(user.getId(), workspace.getId(), PageRequest.of(0,8));

        return WorkspaceConverter.toRecentDocumentList(
                recentDocumentList.stream()
                        .map(recentDocument -> {
                            String presignedUrl = amazonS3Manager.generatePresignedUrl(recentDocument.getThumbnailKeyName());
                            return WorkspaceConverter.toRecentDocument(recentDocument, presignedUrl);
                        })
                        .toList()
        );
    }

    @Override
    public Workspace getWorkspace(Long workspaceId) {
        return workspacePort.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceHandler(ErrorStatus.WORKSPACE_NOT_FOUND));
    }
}

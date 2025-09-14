package com.haru.api.workspace.application.converter;

import com.haru.api.common_library.domain.Documentable;
import com.haru.api.user.domain.UserDocumentLastOpened;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;

import java.util.List;

public class WorkspaceConverter {

    public static WorkspaceResponseDTO.Workspace toWorkspaceDTO(Workspace workspace, String presignedUrl) {
        return WorkspaceResponseDTO.Workspace.builder()
                .workspaceId(workspace.getId())
                .title(workspace.getTitle())
                .imageUrl(presignedUrl)
                .build();
    }

    public static WorkspaceResponseDTO.Document toDocument(UserDocumentLastOpened document) {
        return WorkspaceResponseDTO.Document.builder()
                .documentId(document.getId().getDocumentId())
                .title(document.getTitle())
                .documentType(document.getId().getDocumentType())
                .lastOpened(document.getLastOpened())
                .build();
    }

    public static WorkspaceResponseDTO.DocumentList toDocumentList(List<WorkspaceResponseDTO.Document> documentList) {
        return WorkspaceResponseDTO.DocumentList.builder()
                .documents(documentList)
                .build();
    }

    public static WorkspaceResponseDTO.InvitationAcceptResult toInvitationAcceptResult(boolean isSuccess, boolean isAlreadyRegistered, Workspace workspace) {
        return WorkspaceResponseDTO.InvitationAcceptResult.builder()
                .isSuccess(isSuccess)
                .isAlreadyRegistered(isAlreadyRegistered)
                .workspaceId(workspace.getId())
                .build();
    }

    public static WorkspaceResponseDTO.DocumentSidebar toDocumentSidebar(UserDocumentLastOpened document) {
        return WorkspaceResponseDTO.DocumentSidebar.builder()
                .documentId(document.getId().getDocumentId())
                .documentType(document.getId().getDocumentType())
                .title(document.getTitle())
                .build();
    }

    public static WorkspaceResponseDTO.DocumentSidebarList toDocumentSidebarList(List<WorkspaceResponseDTO.DocumentSidebar> documentList) {
        return WorkspaceResponseDTO.DocumentSidebarList.builder()
                .documents(documentList)
                .build();
    }

    public static WorkspaceResponseDTO.DocumentCalendar toDocumentCalender(Documentable document) {
        return WorkspaceResponseDTO.DocumentCalendar.builder()
                .documentId(document.getId())
                .title(document.getTitle())
                .documentType(document.getDocumentType())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public static WorkspaceResponseDTO.DocumentCalendarList toDocumentCalendarList(
            List<WorkspaceResponseDTO.DocumentCalendar> documentCalendarList) {
        return WorkspaceResponseDTO.DocumentCalendarList.builder()
                .documentList(documentCalendarList)
                .build();
    }

    public static WorkspaceResponseDTO.WorkspaceEditPage toWorkspaceEditPage(Workspace workspace, List<UserResponseDTO.MemberInfo> memberInfoList, String imageUrl) {
        return WorkspaceResponseDTO.WorkspaceEditPage.builder()
                .title(workspace.getTitle())
                .imageUrl(imageUrl)
                .members(memberInfoList)
                .build();
    }

    public static WorkspaceResponseDTO.RecentDocument toRecentDocument(UserDocumentLastOpened userDocumentLastOpened, String thumbnailUrl) {
        return WorkspaceResponseDTO.RecentDocument.builder()
                .documentId(userDocumentLastOpened.getId().getDocumentId())
                .title(userDocumentLastOpened.getTitle())
                .documentType(userDocumentLastOpened.getId().getDocumentType())
                .thumbnailUrl(thumbnailUrl)
                .lastOpened(userDocumentLastOpened.getLastOpened())
                .build();
    }

    public static WorkspaceResponseDTO.RecentDocumentList toRecentDocumentList(List<WorkspaceResponseDTO.RecentDocument> recentDocumentList) {
        return WorkspaceResponseDTO.RecentDocumentList.builder()
                .documents(recentDocumentList)
                .build();
    }
}

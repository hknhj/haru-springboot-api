package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;

import java.time.LocalDate;

public interface WorkspaceQueryUseCase {

    WorkspaceResponseDTO.DocumentList getDocuments(User user, Workspace workspace, String title);

    WorkspaceResponseDTO.DocumentSidebarList getDocumentWithoutLastOpenedList(User user, Workspace workspace);

    WorkspaceResponseDTO.DocumentCalendarList getDocumentForCalendar(User user, Workspace workspace, LocalDate startDate, LocalDate endDate);

    WorkspaceResponseDTO.WorkspaceEditPage getEditPage(User user, Workspace workspace);

    WorkspaceResponseDTO.RecentDocumentList getRecentDocuments(User user, Workspace workspace);
}

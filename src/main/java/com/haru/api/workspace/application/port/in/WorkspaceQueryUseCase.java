package com.haru.api.workspace.application.port.in;

import com.haru.api.user.domain.User;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;

import java.time.LocalDate;

public interface WorkspaceQueryUseCase {

    /**
     * 제목으로 워크스페이스에 속해있는 문서를 검색하는 메서드
     *
     * @param user
     * @param workspace
     * @param title
     * @return
     */
    WorkspaceResponseDTO.DocumentList getDocumentsByTitle(User user, Workspace workspace, String title);

    /**
     * 사이드바에 최신문서를 보여주기 위해 조회하는 메서드
     *
     * @param user
     * @param workspace
     * @return
     */
    WorkspaceResponseDTO.DocumentSidebarList getDocumentsForSidebar(User user, Workspace workspace);

    /**
     * 캘린더에 문서를 보여주기 위한 메서드
     *
     * @param user
     * @param workspace
     * @param startDate
     * @param endDate
     * @return
     */
    WorkspaceResponseDTO.DocumentCalendarList getDocumentForCalendar(User user, Workspace workspace, LocalDate startDate, LocalDate endDate);

    /**
     * 워크스페이스 수정 페이지 조회를 위한 메서드
     *
     * @param user
     * @param workspace
     * @return
     */
    WorkspaceResponseDTO.WorkspaceEditPage getEditPage(User user, Workspace workspace);

    /**
     * 메인페이지에 최근 문서를 보여주기 위한 메서드
     *
     * @param user
     * @param workspace
     * @return
     */
    WorkspaceResponseDTO.RecentDocumentList getRecentDocuments(User user, Workspace workspace);
}

package com.haru.api.common_library.application.port.in;

import com.haru.api.common_library.domain.Documentable;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentQueryUseCase {

    /**
     * 워크스페이스에 속한 문서를 조회하는 메서드
     *
     * @param workspaceId
     * @return
     */
    List<Documentable> getDocumentsByWorkspaceId(Long workspaceId);

    /**
     * 워크스페이스에 속한 문서중에서 시작 날짜와 종료 날짜에 생성된 문서를 조회하는 메서드
     *
     * @param workspaceId
     * @param startDate
     * @param endDate
     * @return
     */
    List<Documentable> getAllDocumentsForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

}

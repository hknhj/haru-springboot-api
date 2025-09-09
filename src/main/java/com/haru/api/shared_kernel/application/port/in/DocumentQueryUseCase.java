package com.haru.api.shared_kernel.application.port.in;

import com.haru.api.shared_kernel.domain.Documentable;

import java.util.List;

public interface DocumentQueryUseCase {

    /**
     * workspace에 속한 문서를 조회하는 메서드
     *
     * @param workspaceId
     * @return
     */
    List<Documentable> getDocumentsByWorkspaceId(Long workspaceId);

}

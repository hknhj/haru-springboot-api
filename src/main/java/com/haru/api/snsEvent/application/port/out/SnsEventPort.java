package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.workspace.domain.Workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SnsEventPort {

    List<SnsEvent> findAllByWorkspaceId(Long workspaceId);

    List<SnsEvent> findAllByWorkspaceOrderByUpdatedAtDesc(Workspace foundWorkspace);

    List<SnsEvent> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<SnsEvent> findSnsEventByIdIfUserHasAccess(Long userId, Long snsEventId);

}

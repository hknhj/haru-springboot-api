package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.infrastructure.jpa.SnsEventJpaRepository;
import com.haru.api.workspace.domain.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SnsEventPersistenceAdapter implements SnsEventPort {

    private final SnsEventJpaRepository snsEventJpaRepository;

    @Override
    public List<SnsEvent> findAllByWorkspaceId(Long workspaceId) {
        return snsEventJpaRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    public List<SnsEvent> findAllByWorkspaceOrderByUpdatedAtDesc(Workspace foundWorkspace) {
        return snsEventJpaRepository.findAllByWorkspaceOrderByUpdatedAtDesc(foundWorkspace);
    }

    @Override
    public List<SnsEvent> findAllForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate) {
        return snsEventJpaRepository.findAllForCalendars(workspaceId, startDate, endDate);
    }

    @Override
    public Optional<SnsEvent> findSnsEventByIdIfUserHasAccess(Long userId, Long snsEventId) {
        return snsEventJpaRepository.findSnsEventByIdIfUserHasAccess(userId, snsEventId);
    }
}

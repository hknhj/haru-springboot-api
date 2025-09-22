package com.haru.api.global.documentFinder;

import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.snsEvent.infrastructure.jpa.SnsEventJpaRepository;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnsEventFinder implements DocumentFinder {

    private final SnsEventJpaRepository snsEventJpaRepository;

    @Override
    public DocumentType getSupportType() {
        return DocumentType.SNS_EVENT_ASSISTANT;
    }

    @Override
    public Object findById(Object id) {
        return snsEventJpaRepository.findById((Long) id)
                .orElseThrow(() -> new SnsEventHandler(ErrorStatus.SNS_EVENT_NOT_FOUND));
    }
}

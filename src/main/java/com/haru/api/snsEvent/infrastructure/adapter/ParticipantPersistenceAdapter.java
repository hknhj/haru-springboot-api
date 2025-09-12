package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.infrastructure.jpa.ParticipantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParticipantPersistenceAdapter implements ParticipantPort {

    private final ParticipantJpaRepository participantJpaRepository;

    @Override
    public List<Participant> findAllBySnsEvent(SnsEvent foundSnsEvent) {
        return participantJpaRepository.findAllBySnsEvent(foundSnsEvent);
    }
}

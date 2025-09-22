package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.infrastructure.jpa.ParticipantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParticipantPersistenceAdapter implements ParticipantPort {

    private final ParticipantJpaRepository participantJpaRepository;

    @Override
    public void saveAll(List<Participant> participants) {
        participantJpaRepository.saveAll(participants);
    }

    @Override
    public List<Participant> findAllBySnsEventId(Long snsEventId) {
        return participantJpaRepository.findAllBySnsEventId(snsEventId);
    }
}

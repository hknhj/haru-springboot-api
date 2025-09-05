package com.haru.api.snsEvent.infrastructure;

import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findAllBySnsEvent(SnsEvent foundSnsEvent);
}

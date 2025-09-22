package com.haru.api.snsEvent.infrastructure.jpa;

import com.haru.api.snsEvent.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantJpaRepository extends JpaRepository<Participant, Long> {

    List<Participant> findAllBySnsEventId(Long snsEventId);

}

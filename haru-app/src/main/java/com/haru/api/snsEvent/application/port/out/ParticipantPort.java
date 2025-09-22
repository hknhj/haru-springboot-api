package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.Participant;

import java.util.List;

public interface ParticipantPort {

    void saveAll(List<Participant> participants);

    List<Participant> findAllBySnsEventId(Long snsEventId);

}

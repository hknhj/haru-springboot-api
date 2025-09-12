package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;

import java.util.List;

public interface ParticipantPort {

    List<Participant> findAllBySnsEvent(SnsEvent foundSnsEvent);

}

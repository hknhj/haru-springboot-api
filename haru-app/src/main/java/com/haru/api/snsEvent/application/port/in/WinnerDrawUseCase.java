package com.haru.api.snsEvent.application.port.in;

import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;

public interface WinnerDrawUseCase {

    void getAndSaveParticipantAndWinner(
            SnsEvent createdSnsEvent,
            String accessToken,
            String snsEventLink,
            SnsEventRequestDTO.SnsCondition snsCondition
    );

}

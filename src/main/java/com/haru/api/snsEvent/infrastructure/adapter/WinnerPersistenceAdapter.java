package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.WinnerPort;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.infrastructure.jpa.WinnerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WinnerPersistenceAdapter implements WinnerPort {

    private final WinnerJpaRepository winnerJpaRepository;

    @Override
    public List<Winner> findAllBySnsEvent(SnsEvent foundSnsEvent) {
        return winnerJpaRepository.findAllBySnsEvent(foundSnsEvent);
    }
}

package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.in.SnsEventQueryUseCase;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.infrastructure.jpa.ParticipantJpaRepository;
import com.haru.api.snsEvent.infrastructure.jpa.SnsEventJpaRepository;
import com.haru.api.snsEvent.infrastructure.jpa.WinnerJpaRepository;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.annotation.TrackLastOpened;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnsEventQueryUseCaseImpl implements SnsEventQueryUseCase {

    private final SnsEventJpaRepository snsEventJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final WinnerJpaRepository winnerJpaRepository;

    @Override
    public SnsEventResponseDTO.GetSnsEventListRequest getSnsEventList(User user, Workspace workspace) {

        List<SnsEvent> snsEventList = snsEventJpaRepository.findAllByWorkspaceOrderByUpdatedAtDesc(workspace);

        return SnsEventConverter.toGetSnsEventListRequest(snsEventList);

    }

    @Override
    @TrackLastOpened
    public SnsEventResponseDTO.GetSnsEventRequest getSnsEvent(User user, SnsEvent snsEvent) {

        List<Participant> participantList = participantJpaRepository.findAllBySnsEvent(snsEvent);

        List<Winner> winnerList = winnerJpaRepository.findAllBySnsEvent(snsEvent);

        return SnsEventConverter.toGetSnsEventRequest(
                snsEvent,
                participantList,
                winnerList
        );

    }

    @Override
    public SnsEventResponseDTO.getInstagramAccountName getInstagramAccountName(User user, Workspace workspace) {
        if (workspace.getInstagramAccountName() == null) {
            return SnsEventConverter.toGetInstagramAccountName(
                    ""
            );
        }
        return SnsEventConverter.toGetInstagramAccountName(
                workspace.getInstagramAccountName()
        );
    }
}

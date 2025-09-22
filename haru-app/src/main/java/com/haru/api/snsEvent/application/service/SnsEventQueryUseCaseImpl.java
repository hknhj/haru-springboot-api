package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.in.SnsEventQueryUseCase;
import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.application.port.out.SnsEventPort;
import com.haru.api.snsEvent.application.port.out.WinnerPort;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.annotation.TrackLastOpened;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnsEventQueryUseCaseImpl implements SnsEventQueryUseCase {

    private final SnsEventPort snsEventPort;
    private final ParticipantPort participantPort;
    private final WinnerPort winnerPort;

    @Override
    public SnsEventResponseDTO.GetSnsEventListRequest getSnsEventList(User user, Workspace workspace) {

        List<SnsEvent> snsEventList = snsEventPort.findAllByWorkspaceOrderByUpdatedAtDesc(workspace);

        return SnsEventConverter.toGetSnsEventListRequest(snsEventList);

    }

    @Override
    @TrackLastOpened
    public SnsEventResponseDTO.GetSnsEventRequest getSnsEvent(User user, SnsEvent snsEvent) {

        List<Participant> participantList = participantPort.findAllBySnsEventId(snsEvent.getId());

        List<Winner> winnerList = winnerPort.findAllBySnsEventId(snsEvent.getId());

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

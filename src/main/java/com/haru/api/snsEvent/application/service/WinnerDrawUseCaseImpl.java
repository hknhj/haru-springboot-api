package com.haru.api.snsEvent.application.service;

import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.in.WinnerDrawUseCase;
import com.haru.api.snsEvent.application.port.out.InstagramPort;
import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.application.port.out.WinnerPort;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.SNS_EVENT_LINK_NOT_FOUND;
import static com.haru.api.global.apiPayload.code.status.ErrorStatus.SNS_EVENT_NO_ACCESS_TOKEN;

@Service
@RequiredArgsConstructor
public class WinnerDrawUseCaseImpl implements WinnerDrawUseCase {

    private final InstagramPort instagramPort;
    private final ParticipantFilter participantFilter;
    private final ParticipantPort participantPort;
    private final WinnerPort winnerPort;
    private final PickWinner pickWinner;

    @Override
    public void getAndSaveParticipantAndWinner(
            SnsEvent createdSnsEvent,
            String accessToken,
            String snsEventLink,
            SnsEventRequestDTO.SnsCondition snsCondition
    ) {

        if (accessToken == null || accessToken.isEmpty()) {
            throw new SnsEventHandler(SNS_EVENT_NO_ACCESS_TOKEN);
        }

        SnsEventResponseDTO.InstagramMediaResponse instagramMediaResponse = instagramPort.fetchInstagramMedia(accessToken);

        String[] splitedSnsEventLink = snsEventLink.split("/");
        String requestShortCode = splitedSnsEventLink[splitedSnsEventLink.length - 1];

        List<Participant> filteredCommentList = new ArrayList<>();
        Set<String> filteredCommentSet = new HashSet<>();
        List<Winner> winnerList = new ArrayList<>();

        for (SnsEventResponseDTO.Media media : instagramMediaResponse.getData()) {
            if (requestShortCode.equals(media.getShortcode())) {
                List<SnsEventResponseDTO.Comment> commentList = instagramPort.getComments(media.getId(), accessToken);
                filteredCommentSet.addAll(participantFilter.getFilteredParticipant(commentList, snsCondition));
            }

            if (instagramMediaResponse.getData().size() - 1 == 0) {
                throw new SnsEventHandler(SNS_EVENT_LINK_NOT_FOUND);
            }
        }
        // 참여자 저장
        for (String nickname : filteredCommentSet) {
            Participant participant = SnsEventConverter.toParticipant(nickname);
            participant.setSnsEvent(createdSnsEvent);
            filteredCommentList.add(participant);
        }
        participantPort.saveAll(filteredCommentList);

        // 당첨자 선정 후 저장
        for (String nickname : pickWinner.getWinners(filteredCommentSet, snsCondition.getWinnerCount())) {
            Winner winner = SnsEventConverter.toWinner(nickname);
            winner.setSnsEvent(createdSnsEvent);
            winnerList.add(winner);
        }
        winnerPort.saveAll(winnerList);

    }
}

package com.haru.api.snsEvent.application.service;

import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.infra.api.restTemplate.InstagramOauth2RestTemplate;
import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.application.port.in.LinkInstagramAccountUseCase;
import com.haru.api.snsEvent.domain.enums.InstagramRedirectType;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.workspace.domain.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class LinkInstagramAccountUseCaseImpl implements LinkInstagramAccountUseCase {

    private final InstagramOauth2RestTemplate instagramOauth2RestTemplate;

    private final WorkspaceQueryUseCase workspaceQueryUseCase;

    @Override
    @Transactional
    public SnsEventResponseDTO.LinkInstagramAccountResponse linkInstagramAccount
            (String code,
             Workspace workspace,
             InstagramRedirectType instagramRedirectType
    ) {

        Workspace foundWorkspace = workspaceQueryUseCase.getWorkspace(workspace.getId());

        String shortLivedAccessToken;
        String longLivedAccessToken;
        Map<String, Object> userInfo;
        try {
            // 1. Access Token 요청
            shortLivedAccessToken = instagramOauth2RestTemplate.getShortLivedAccessTokenUrl(
                    code,
                    instagramRedirectType
            );

            // 2. 단기 토큰을 장기(Long-Lived) 토큰으로 교환
            longLivedAccessToken = instagramOauth2RestTemplate.getLongLivedAccessToken(shortLivedAccessToken);

            // 3. 장기 토큰으로 사용자 계정 정보 요청
            userInfo = instagramOauth2RestTemplate.getInstagramAccountInfo(longLivedAccessToken);
        } catch (Exception e) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        }
        // 4. 워크스페이스에 인스타그램 계정 정보 저장
        String instagramId = (String) userInfo.get("user_id");

        if (foundWorkspace.getInstagramId() != null && foundWorkspace.getInstagramId().equals(instagramId)) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_ALREADY_LINKED);
        }
        foundWorkspace.saveInstagramId(instagramId);
        foundWorkspace.saveInstagramAccessToken(longLivedAccessToken);
        foundWorkspace.saveInstagramAccountName((String) userInfo.get("username"));
        return SnsEventConverter.toLinkInstagramAccountResponse((String) userInfo.get("username"));
    }
}

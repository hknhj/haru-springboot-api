package com.haru.api.snsEvent.application.service;

import com.haru.api.global.annotation.CreateDocument;
import com.haru.api.snsEvent.application.port.in.UploadFileAndThumbnail;
import com.haru.api.snsEvent.application.port.out.*;
import com.haru.api.snsEvent.application.port.in.SnsEventCommandUseCase;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.snsEvent.application.converter.SnsEventConverter;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.snsEvent.domain.enums.InstagramRedirectType;
import com.haru.api.snsEvent.domain.enums.ListType;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.infrastructure.jpa.WorkspaceJpaRepository;
import com.haru.api.global.annotation.DeleteDocument;
import com.haru.api.global.annotation.UpdateDocument;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceHandler;
import com.haru.api.infra.api.restTemplate.InstagramOauth2RestTemplate;
import com.haru.api.infra.s3.AmazonS3Manager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsEventCommandUseCaseImpl implements SnsEventCommandUseCase {

    private final InstagramPort instagramPort;
    private final SnsEventPort snsEventPort;
    private final ParticipantPort participantPort;
    private final WinnerPort winnerPort;

    private final ParticipantFilter participantFilter;

    private final UploadFileAndThumbnail uploadFileAndThumbnail;

    private final WorkspaceJpaRepository workspaceJpaRepository;
    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;

    private final UserDocumentLastOpenedCommandUseCase userDocumentLastOpenedCommandUseCase;
    private final InstagramOauth2RestTemplate instagramOauth2RestTemplate;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    @Transactional
    @CreateDocument(documentType = DocumentType.SNS_EVENT_ASSISTANT)
    public SnsEventResponseDTO.CreateSnsEventResponse createSnsEvent(
            User user,
            Workspace workspace,
            SnsEventRequestDTO.CreateSnsRequest request
    ) {

        Workspace foundWorkspace = workspaceJpaRepository.findById(workspace.getId())
                .orElseThrow(() -> new WorkspaceHandler(WORKSPACE_NOT_FOUND));

        // SNS 이벤트 생성
        SnsEvent createdSnsEvent = SnsEventConverter.toSnsEvent(request, user);
        createdSnsEvent.setWorkspace(foundWorkspace);

        // Instagram API 호출 후 참여자 리스트, 당첨자 리스트 생성 및 저장
        String accessToken = foundWorkspace.getInstagramAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new SnsEventHandler(SNS_EVENT_NO_ACCESS_TOKEN);
        }

        SnsEventResponseDTO.InstagramMediaResponse instagramMediaResponse = instagramPort.fetchInstagramMedia(accessToken);

        String[] splitedSnsEventLink = request.getSnsEventLink().split("/");
        String requestShortCode = splitedSnsEventLink[splitedSnsEventLink.length - 1];

        List<Participant> filteredCommentList = new ArrayList<>();
        Set<String> filteredCommentSet = new HashSet<>();
        List<Winner> winnerList = new ArrayList<>();

        for (SnsEventResponseDTO.Media media : instagramMediaResponse.getData()) {
            if (requestShortCode.equals(media.getShortcode())) {
                List<SnsEventResponseDTO.Comment> commentList = instagramPort.getComments(media.getId(), accessToken);
                filteredCommentSet.addAll(participantFilter.getFilteredParticipant(commentList, request.getSnsCondition()));
            }
            // 마지막까지 돌았는데 shortcode파싱해둔것과 일치하는게 없다면 error처리해야됨.
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
        for (String nickname : pickWinners(filteredCommentSet, request.getSnsCondition().getWinnerCount())) {
            Winner winner = SnsEventConverter.toWinner(nickname);
            winner.setSnsEvent(createdSnsEvent);
            winnerList.add(winner);
        }
        winnerPort.saveAll(winnerList);

        SnsEvent savedSnsEvent = snsEventPort.save(createdSnsEvent);

        // PDF, DOCX파일 바이트 배열로 생성 및 썸네일 생성 & 업로드 / DB에 keyName저장
        String thumbnailKeyName = uploadFileAndThumbnail.createAndUploadListFileAndThumbnail(savedSnsEvent);

        // sns event 썸네일 key name 초기화
        savedSnsEvent.initThumbnailKeyName(thumbnailKeyName);

        return SnsEventResponseDTO.CreateSnsEventResponse.builder()
                .snsEventId(createdSnsEvent.getId())
                .build();
    }


    @Override
    @Transactional
    public SnsEventResponseDTO.LinkInstagramAccountResponse getInstagramAccessTokenAndAccount(
            String code,
            Workspace workspace,
            InstagramRedirectType instagramRedirectType
    ) {
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
            log.error("Instagram OAuth2 처리 중 오류 발생: {}", e.getMessage());
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        }
        // 4. 워크스페이스에 인스타그램 계정 정보 저장
        String instagramId = (String) userInfo.get("user_id");
        Workspace foundWorkspace = workspaceJpaRepository.findById(workspace.getId())
                .orElseThrow(() -> new WorkspaceHandler(WORKSPACE_NOT_FOUND));
        if (foundWorkspace.getInstagramId() != null && foundWorkspace.getInstagramId().equals(instagramId)) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_ALREADY_LINKED);
        }
        foundWorkspace.saveInstagramId(instagramId);
        foundWorkspace.saveInstagramAccessToken(longLivedAccessToken);
        foundWorkspace.saveInstagramAccountName((String) userInfo.get("username"));
        return SnsEventConverter.toLinkInstagramAccountResponse((String) userInfo.get("username"));
    }
  
    @Override
    @Transactional
    @UpdateDocument
    public void updateSnsEventTitle(
            User user,
            SnsEvent snsEvent,
            SnsEventRequestDTO.UpdateSnsEventRequest request
    ) {

        UserWorkspace foundUserWorkspace = userWorkspaceJpaRepository.findByWorkspaceAndAuth(snsEvent.getWorkspace(), Auth.ADMIN)
                .orElseThrow(() -> new MemberHandler(WORKSPACE_CREATOR_NOT_FOUND));

        // 수정 권한 확인 (워크스페이스 생성자 혹은 SNS 이벤트의 생성자만 수정 가능)
        if (!foundUserWorkspace.getUser().getId().equals(user.getId()) || !snsEvent.getCreator().getId().equals(user.getId())) {
            throw new SnsEventHandler(SNS_EVENT_NO_AUTHORITY);
        }

        snsEvent.updateTitle(request.getTitle());
        SnsEvent savedSnsEvent = snsEventPort.save(snsEvent);

        // S3문서 제목, S3 문서내 제목, 썸네일 이미지의 제목 변경
        deleteS3FileAndThumnailImage(savedSnsEvent);

        String thumbnailKeyName = uploadFileAndThumbnail.createAndUploadListFileAndThumbnail(savedSnsEvent);
        // sns event 썸네일 key name 초기화
        savedSnsEvent.initThumbnailKeyName(thumbnailKeyName);

        // SNS Event 제목 수정 시 워크스페이스에 속해있는 모든 유저에 대해 썸네일 이미지 키 수정
        List<User> usersInWorkspace = userWorkspaceJpaRepository.findUsersByWorkspaceId(savedSnsEvent.getWorkspace().getId());
        userDocumentLastOpenedCommandUseCase.updateRecordsTitleAndThumbnailForWorkspaceUsers(savedSnsEvent, request);
    }

    @Override
    @Transactional
    @DeleteDocument
    public void deleteSnsEvent(
            User user,
            SnsEvent snsEvent
    ) {

        UserWorkspace foundUserWorkspace = userWorkspaceJpaRepository.findByWorkspaceAndAuth(snsEvent.getWorkspace(), Auth.ADMIN)
                .orElseThrow(() -> new MemberHandler(WORKSPACE_CREATOR_NOT_FOUND));

        // 수정 권한 확인 (워크스페이스 생성자 혹은 SNS 이벤트의 생성자만 삭제 가능)
        if (!foundUserWorkspace.getUser().getId().equals(user.getId()) || !snsEvent.getCreator().getId().equals(user.getId())) {
            throw new SnsEventHandler(SNS_EVENT_NO_AUTHORITY);
        }

        // S3의 문서 및 썸네일 이미지 삭제
        deleteS3FileAndThumnailImage(snsEvent);

        snsEventPort.delete(snsEvent);

    }

    @Override
    public SnsEventResponseDTO.ListDownLoadLinkResponse downloadList(
            User user,
            SnsEvent snsEvent,
            ListType listType,
            Format format
    ) {
        String downloadLink = "";

        String snsEventTitle = snsEvent.getTitle();
        if (listType == ListType.PARTICIPANT) {
            if (format == Format.PDF) {
                String keyName = snsEvent.getKeyNameParticipantPdf();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = amazonS3Manager.generatePresignedUrlForDownloadPdfAndWord(keyName, snsEventTitle + "_참여자_리스트.pdf");
            } else if (format == Format.DOCX) {
                String keyName = snsEvent.getKeyNameParticipantWord();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = amazonS3Manager.generatePresignedUrlForDownloadPdfAndWord(keyName, snsEventTitle + "_참여자_리스트.docx");
            } else {
                throw new SnsEventHandler(SNS_EVENT_WRONG_FORMAT);
            }
        } else if (listType == ListType.WINNER) {
            if (format == Format.PDF) {
                String keyName = snsEvent.getKeyNameWinnerPdf();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = amazonS3Manager.generatePresignedUrlForDownloadPdfAndWord(keyName, snsEventTitle + "_당첨자_리스트.pdf");
            } else if (format == Format.DOCX) {
                String keyName = snsEvent.getKeyNameWinnerWord();
                if (keyName == null || keyName.isEmpty()) {
                    throw new SnsEventHandler(SNS_EVENT_LIST_KEYNAME_NOT_FOUND);
                }
                downloadLink = amazonS3Manager.generatePresignedUrlForDownloadPdfAndWord(keyName, snsEventTitle + "_당첨자_리스트.docx");
            } else {
                throw new SnsEventHandler(SNS_EVENT_WRONG_FORMAT);
            }
        } else {
            throw new SnsEventHandler(SNS_EVENT_WRONG_LIST_TYPE);
        }
        return SnsEventResponseDTO.ListDownLoadLinkResponse.builder()
                .downloadLink(downloadLink)
                .build();
    }

    private List<String> pickWinners(Set<String> participants, int n) {
        List<String> list = new ArrayList<>(participants); // Set → List로 변환
        Collections.shuffle(list); // 무작위 섞기

        if (n >= list.size()) {
            return list; // 참가자가 n보다 적으면 전원 반환
        }

        return list.subList(0, n); // 앞에서 n개만 추출
    }

    private void deleteS3FileAndThumnailImage(SnsEvent snsEvent) {
        amazonS3Manager.deleteFile(snsEvent.getKeyNameParticipantPdf());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameParticipantWord());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameWinnerPdf());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameWinnerWord());
        amazonS3Manager.deleteFile(snsEvent.getThumbnailKeyName());
    }
}

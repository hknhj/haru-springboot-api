package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.snsEvent.application.port.out.InstagramPort;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.*;
import static com.haru.api.global.apiPayload.code.status.ErrorStatus.SNS_EVENT_INSTAGRAM_API_ERROR;

@Service
@RequiredArgsConstructor
public class InstagramAdapter implements InstagramPort {

    private final RestTemplate restTemplate;

    @Override
    public SnsEventResponseDTO.InstagramMediaResponse fetchInstagramMedia(String accessToken) {

        String baseUrl = "https://graph.instagram.com/me/media";
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("fields", "shortcode")
                .queryParam("access_token", accessToken)
                .toUriString();

        // 가져오는 값 없거나 error뜨면 error처리해야됨.
        try {
            SnsEventResponseDTO.InstagramMediaResponse response = restTemplate.getForObject(url, SnsEventResponseDTO.InstagramMediaResponse.class);
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_NO_MEDIA);
            }
            return response;
        } catch (HttpClientErrorException e) {
            // 4xx 에러 코드 처리 (예: 인증 실패, 권한 부족)
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_AUTH_ERROR);
        } catch (RestClientException e) {
            // 네트워크 오류, 5xx 서버 에러 등 기타 예외 처리
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        } catch (Exception e) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        }
    }

    @Override
    public List<SnsEventResponseDTO.Comment> getComments(String mediaId, String accessToken) {
        String baseUrl = "https://graph.instagram.com/" + mediaId + "/comments";
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParam("fields", "from,text,timestamp")
                .queryParam("limit", 50)
                .queryParam("access_token", accessToken)
                .toUriString();

        // 가져오는 값 없거나 error뜨면 error처리해야됨.
        try {
            SnsEventResponseDTO.InstagramCommentResponse response = restTemplate.getForObject(url, SnsEventResponseDTO.InstagramCommentResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                System.out.println("게시물에 댓글이 없습니다.");
                throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_NO_COMMENT);
            }
            return response.getData();
        } catch (HttpClientErrorException e) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        } catch (RestClientException e) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        } catch (Exception e) {
            throw new SnsEventHandler(SNS_EVENT_INSTAGRAM_API_ERROR);
        }
    }
}

package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;

import java.util.List;

public interface InstagramPort {

    /**
     * 인스타그램 API를 사용해서 게시물 정보를 가져오는 메서드
     *
     * @param accessToken
     * @return
     */
    SnsEventResponseDTO.InstagramMediaResponse fetchInstagramMedia(String accessToken);

    /**
     * 인스타그램 API를 사용해서 게시물의 댓글을 가져오는 메서드
     *
     * @param mediaId
     * @param accessToken
     * @return
     */
    List<SnsEventResponseDTO.Comment> getComments(String mediaId, String accessToken);

}

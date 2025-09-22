package com.haru.term.application;

import com.haru.term.domain.Term;
import com.haru.common.dto.term.TermResponseDTO;

public class TermConverter {

    public static TermResponseDTO.TermDetail toTermsDetailDTO(Term term) {
        return TermResponseDTO.TermDetail.builder()
                .type(term.getType())
                .title(term.getTitle())
                .content(term.getContent())
                .build();
    }
}

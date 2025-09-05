package com.haru.api.term.application.converter;

import com.haru.api.term.presentation.dto.TermResponseDTO;
import com.haru.api.term.domain.Term;
import org.springframework.stereotype.Component;

@Component
public class TermConverter {

    public static TermResponseDTO.TermDetail toTermsDetailDTO(Term term) {
        return TermResponseDTO.TermDetail.builder()
                .type(term.getType())
                .title(term.getTitle())
                .content(term.getContent())
                .build();
    }
}

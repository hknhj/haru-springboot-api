package com.haru.api.term.presentation.dto;

import com.haru.api.term.domain.enums.TermType;
import lombok.Builder;
import lombok.Getter;

public class TermResponseDTO {

    @Getter
    @Builder
    public static class TermDetail {
        TermType type;
        String title;
        String content;
    }
}

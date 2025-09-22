package com.haru.api.common_library.dto.term;

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

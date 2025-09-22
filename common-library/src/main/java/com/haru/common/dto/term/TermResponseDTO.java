package com.haru.common.dto.term;

import com.haru.common.type.TermType;
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

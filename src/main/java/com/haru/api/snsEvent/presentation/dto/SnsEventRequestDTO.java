package com.haru.api.snsEvent.presentation.dto;

import com.haru.api.shared_kernel.domain.DocumentModifier;
import lombok.*;

import java.time.LocalDateTime;

public class SnsEventRequestDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateSnsRequest {
        private String title;
        private String snsEventLink;
        private SnsCondition snsCondition;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SnsCondition {
        private Integer winnerCount;
        private Boolean isPeriod;
        private LocalDateTime period;
        private Boolean isKeyword;
        private String keyword;
        private Boolean isTagged;
        private Integer tageCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSnsEventRequest implements DocumentModifier {
        private String title;
        private String thumbnailKeyName;
    }
}

package com.haru.api.meeting.presentation.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.haru.api.global.common.entity.DocumentModifier;
import com.haru.api.global.util.json.ToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MeetingRequestDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createMeetingRequest{
        @Schema(type = "string")
        @JsonDeserialize(using = ToLongDeserializer.class)
        private Long workspaceId;
        private String title;
    }
    @Getter
    public static class updateTitle implements DocumentModifier {
        private String title;
        private String thumbnailKeyName;
    }

    @Getter
    public static class meetingProceedingRequest{
        private String proceeding;
    }
}

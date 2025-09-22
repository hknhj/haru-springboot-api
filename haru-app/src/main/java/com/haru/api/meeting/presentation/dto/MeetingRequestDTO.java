package com.haru.api.meeting.presentation.dto;

import com.haru.api.common_library.domain.DocumentModifier;
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
        private String title;
    }

    @Getter
    @Builder
    public static class updateTitle implements DocumentModifier {
        private String title;
        private String thumbnailKeyName;
    }

    @Getter
    @Builder
    public static class meetingProceedingRequest{
        private String proceeding;
    }
}

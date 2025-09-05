package com.haru.api.moodTracker.application.port.in;

public interface MoodTrackerMailUseCase {
    void sendSurveyLinkToEmail(
            Long moodTrackerId,
            String mailTitle,
            String mailContent
    );
}

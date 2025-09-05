package com.haru.api.moodTracker.application.port.in;

import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.snsEvent.domain.enums.Format;

public interface MoodTrackerReportUseCase {
    void generateReport(Long moodTrackerId);
    void generateAndUploadReportFileAndThumbnail(
            Long moodTrackerId
    );
    void updateAndUploadReportFileAndThumbnail(
            Long moodTrackerId
    );
    void deleteReportFileAndThumbnail(
            Long moodTrackerId
    );
    String generateDownloadLink(
            MoodTracker moodTracker,
            Format format
    );
}

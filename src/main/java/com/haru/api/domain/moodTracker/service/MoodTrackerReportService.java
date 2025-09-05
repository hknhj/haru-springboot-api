package com.haru.api.domain.moodTracker.service;

import com.haru.api.domain.moodTracker.entity.MoodTracker;
import com.haru.api.snsEvent.domain.enums.Format;

public interface MoodTrackerReportService {
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

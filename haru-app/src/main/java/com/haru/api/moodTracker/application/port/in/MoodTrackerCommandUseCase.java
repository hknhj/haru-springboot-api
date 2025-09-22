package com.haru.api.moodTracker.application.port.in;

import com.haru.api.moodTracker.presentation.dto.MoodTrackerRequestDTO;
import com.haru.api.moodTracker.presentation.dto.MoodTrackerResponseDTO;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

public interface MoodTrackerCommandUseCase {
    MoodTrackerResponseDTO.CreateResult create(
            User user,
            Workspace workspace,
            MoodTrackerRequestDTO.CreateRequest request
    );
    void updateTitle(
            User user,
            MoodTracker moodTracker,
            MoodTrackerRequestDTO.UpdateTitleRequest request
    );
    void delete(
            User user,
            MoodTracker moodTracker
    );
    void sendSurveyLink(
            MoodTracker moodTracker
    );
    void submitSurveyAnswers(
            Long moodTrackerId,
            MoodTrackerRequestDTO.SurveyAnswerList request
    );
    MoodTrackerResponseDTO.ReportDownLoadLinkResponse getDownloadLink(
            User user,
            MoodTracker moodTracker,
            Format format
    );
    void generateReportTest(
            MoodTracker moodTracker
    );
    void generateReportFileAndThumbnailTest(
            MoodTracker moodTracker
    );
}

package com.haru.api.moodTracker.application.port.in;

import com.haru.api.moodTracker.presentation.dto.MoodTrackerResponseDTO;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

public interface MoodTrackerQueryUseCase {
    MoodTrackerResponseDTO.PreviewList getPreviewList(User user, Workspace workspace);

    MoodTrackerResponseDTO.BaseResult getBaseResult(Long moodTrackerId);

    MoodTrackerResponseDTO.QuestionResult getQuestionResult(Long moodTrackerId);

    MoodTrackerResponseDTO.ReportResult getReportResult(User user, MoodTracker moodTracker);

    MoodTrackerResponseDTO.ResponseResult getResponseResult(User user, MoodTracker moodTracker);
}

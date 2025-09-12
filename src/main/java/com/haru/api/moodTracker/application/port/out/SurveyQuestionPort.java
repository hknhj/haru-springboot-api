package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.SurveyQuestion;

import java.util.List;

public interface SurveyQuestionPort {

    SurveyQuestion save(SurveyQuestion surveyQuestion);

    List<SurveyQuestion> findAllByMoodTrackerId(Long moodTrackerId);

}

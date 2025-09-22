package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.SurveyQuestionPort;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.infrastructure.jpa.SurveyQuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SurveyQuestionPersistenceAdapter implements SurveyQuestionPort {

    private final SurveyQuestionJpaRepository surveyQuestionJpaRepository;

    @Override
    public SurveyQuestion save(SurveyQuestion surveyQuestion) {
        return surveyQuestionJpaRepository.save(surveyQuestion);
    }

    @Override
    public List<SurveyQuestion> findAllByMoodTrackerId(Long moodTrackerId) {
        return surveyQuestionJpaRepository.findAllByMoodTrackerId(moodTrackerId);
    }
}

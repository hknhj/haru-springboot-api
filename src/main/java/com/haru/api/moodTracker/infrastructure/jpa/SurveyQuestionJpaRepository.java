package com.haru.api.moodTracker.infrastructure.jpa;

import com.haru.api.moodTracker.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionJpaRepository extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findAllByMoodTrackerId(Long moodTrackerId);
}

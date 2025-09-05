package com.haru.api.moodTracker.infrastructure;

import com.haru.api.moodTracker.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findAllByMoodTrackerId(Long moodTrackerId);
}

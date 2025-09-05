package com.haru.api.moodTracker.infrastructure;

import com.haru.api.moodTracker.domain.SubjectiveAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectiveAnswerRepository extends JpaRepository<SubjectiveAnswer, Long> {
    List<SubjectiveAnswer> findAllBySurveyQuestionIn(List<SurveyQuestion> questions);
}

package com.haru.api.moodTracker.infrastructure;

import com.haru.api.moodTracker.domain.MultipleChoiceAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceAnswerRepository extends JpaRepository<MultipleChoiceAnswer, Long> {
    List<MultipleChoiceAnswer> findAllByMultipleChoice_SurveyQuestionIn(List<SurveyQuestion> questions);
}

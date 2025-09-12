package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.MultipleChoiceAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;

import java.util.List;

public interface MultipleChoiceAnswerPort {

    void saveAll(List<MultipleChoiceAnswer> multipleChoiceAnswers);

    List<MultipleChoiceAnswer> findAllByMultipleChoice_SurveyQuestionIn(List<SurveyQuestion> questions);

}

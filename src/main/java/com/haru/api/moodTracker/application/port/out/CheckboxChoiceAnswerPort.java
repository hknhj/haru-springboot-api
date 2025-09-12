package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.CheckboxChoiceAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;

import java.util.List;

public interface CheckboxChoiceAnswerPort {

    void saveAll(List<CheckboxChoiceAnswer> checkboxChoiceAnswers);

    List<CheckboxChoiceAnswer> findAllByCheckboxChoice_SurveyQuestionIn(List<SurveyQuestion> questions);

}

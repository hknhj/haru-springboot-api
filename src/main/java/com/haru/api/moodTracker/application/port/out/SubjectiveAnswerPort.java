package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.SubjectiveAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;

import java.util.List;

public interface SubjectiveAnswerPort {

    void saveAll(List<SubjectiveAnswer> subjectAnswers);

    List<SubjectiveAnswer> findAllBySurveyQuestionIn(List<SurveyQuestion> questions);

}

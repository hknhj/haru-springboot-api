package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.CheckboxChoiceAnswerPort;
import com.haru.api.moodTracker.domain.CheckboxChoiceAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.infrastructure.jpa.CheckboxChoiceAnswerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckboxChoiceAnswerPersistenceAdapter implements CheckboxChoiceAnswerPort {

    private final CheckboxChoiceAnswerJpaRepository checkboxChoiceAnswerJpaRepository;

    @Override
    public void saveAll(List<CheckboxChoiceAnswer> checkboxChoiceAnswers) {
        checkboxChoiceAnswerJpaRepository.saveAll(checkboxChoiceAnswers);
    }

    @Override
    public List<CheckboxChoiceAnswer> findAllByCheckboxChoice_SurveyQuestionIn(List<SurveyQuestion> questions) {
        return checkboxChoiceAnswerJpaRepository.findAllByCheckboxChoice_SurveyQuestionIn(questions);
    }
}

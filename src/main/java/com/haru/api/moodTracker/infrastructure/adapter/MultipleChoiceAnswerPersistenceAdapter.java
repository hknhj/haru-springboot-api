package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.MultipleChoiceAnswerPort;
import com.haru.api.moodTracker.domain.MultipleChoiceAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.infrastructure.jpa.MultipleChoiceAnswerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MultipleChoiceAnswerPersistenceAdapter implements MultipleChoiceAnswerPort {

    private final MultipleChoiceAnswerJpaRepository multipleChoiceAnswerJpaRepository;

    @Override
    public void saveAll(List<MultipleChoiceAnswer> multipleChoiceAnswers) {
        multipleChoiceAnswerJpaRepository.saveAll(multipleChoiceAnswers);
    }

    @Override
    public List<MultipleChoiceAnswer> findAllByMultipleChoice_SurveyQuestionIn(List<SurveyQuestion> questions) {
        return multipleChoiceAnswerJpaRepository.findAllByMultipleChoice_SurveyQuestionIn(questions);
    }
}

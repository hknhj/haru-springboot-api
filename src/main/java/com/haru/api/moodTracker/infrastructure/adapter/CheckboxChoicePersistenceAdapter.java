package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.CheckboxChoicePort;
import com.haru.api.moodTracker.domain.CheckboxChoice;
import com.haru.api.moodTracker.infrastructure.jpa.CheckboxChoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckboxChoicePersistenceAdapter implements CheckboxChoicePort {

    private final CheckboxChoiceJpaRepository checkboxChoiceJpaRepository;

    @Override
    public void saveAll(List<CheckboxChoice> checkboxChoices) {
        checkboxChoiceJpaRepository.saveAll(checkboxChoices);
    }

    @Override
    public List<CheckboxChoice> findAllByIdInAndSurveyQuestionId(List<Long> ids, Long questionId) {
        return checkboxChoiceJpaRepository.findAllByIdInAndSurveyQuestionId(ids, questionId);
    }
}

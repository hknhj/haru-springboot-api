package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.MultipleChoicePort;
import com.haru.api.moodTracker.domain.MultipleChoice;
import com.haru.api.moodTracker.infrastructure.jpa.MultipleChoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MultipleChoicePersistenceAdapter implements MultipleChoicePort {

    private final MultipleChoiceJpaRepository multipleChoiceJpaRepository;

    @Override
    public void saveAll(List<MultipleChoice> multipleChoices) {
        multipleChoiceJpaRepository.saveAll(multipleChoices);
    }

    @Override
    public Optional<MultipleChoice> findByIdAndSurveyQuestionId(Long id, Long questionId) {
        return multipleChoiceJpaRepository.findByIdAndSurveyQuestionId(id, questionId);
    }
}

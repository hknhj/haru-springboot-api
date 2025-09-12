package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.MultipleChoice;

import java.util.List;
import java.util.Optional;

public interface MultipleChoicePort {

    void saveAll(List<MultipleChoice> multipleChoices);

    // 특정 질문(questionId)에 속한 특정 선택지(id)만 조회
    Optional<MultipleChoice> findByIdAndSurveyQuestionId(Long id, Long questionId);

}

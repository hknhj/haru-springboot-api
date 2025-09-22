package com.haru.api.moodTracker.application.port.out;

import com.haru.api.moodTracker.domain.CheckboxChoice;

import java.util.List;

public interface CheckboxChoicePort {

    void saveAll(List<CheckboxChoice> checkboxChoices);

    // 특정 질문(questionId)에 속한 여러 선택지(ids) 조회
    List<CheckboxChoice> findAllByIdInAndSurveyQuestionId(List<Long> ids, Long questionId);

}

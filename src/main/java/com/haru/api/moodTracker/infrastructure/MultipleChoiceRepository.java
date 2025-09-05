package com.haru.api.moodTracker.infrastructure;

import com.haru.api.moodTracker.domain.MultipleChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MultipleChoiceRepository extends JpaRepository<MultipleChoice, Long> {
    // 특정 질문(questionId)에 속한 특정 선택지(id)만 조회
    Optional<MultipleChoice> findByIdAndSurveyQuestionId(Long id, Long questionId);
}

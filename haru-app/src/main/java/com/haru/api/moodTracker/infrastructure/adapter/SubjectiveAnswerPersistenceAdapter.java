package com.haru.api.moodTracker.infrastructure.adapter;

import com.haru.api.moodTracker.application.port.out.SubjectiveAnswerPort;
import com.haru.api.moodTracker.domain.SubjectiveAnswer;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.infrastructure.jpa.SubjectiveAnswerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubjectiveAnswerPersistenceAdapter implements SubjectiveAnswerPort {

    private final SubjectiveAnswerJpaRepository subjectiveAnswerJpaRepository;

    @Override
    public void saveAll(List<SubjectiveAnswer> subjectAnswers) {
        subjectiveAnswerJpaRepository.saveAll(subjectAnswers);
    }

    @Override
    public List<SubjectiveAnswer> findAllBySurveyQuestionIn(List<SurveyQuestion> questions) {
        return subjectiveAnswerJpaRepository.findAllBySurveyQuestionIn(questions);
    }
}

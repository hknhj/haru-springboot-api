package com.haru.api.moodTracker.application.service;

import com.haru.api.global.annotation.TrackLastOpened;
import com.haru.api.moodTracker.application.port.in.MoodTrackerQueryUseCase;
import com.haru.api.moodTracker.application.port.out.MoodTrackerPort;
import com.haru.api.moodTracker.infrastructure.jpa.SurveyQuestionJpaRepository;
import com.haru.api.moodTracker.application.converter.MoodTrackerConverter;
import com.haru.api.moodTracker.presentation.dto.MoodTrackerResponseDTO;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.domain.enums.MoodTrackerVisibility;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MoodTrackerHandler;
import com.haru.api.global.apiPayload.exception.handler.UserWorkspaceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoodTrackerQueryUseCaseImpl implements MoodTrackerQueryUseCase {

    private final MoodTrackerPort moodTrackerPort;

    private final UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;

    private final SurveyQuestionJpaRepository surveyQuestionJpaRepository;

    @Override
    public MoodTrackerResponseDTO.PreviewList getPreviewList(User user, Workspace workspace) {

        UserWorkspace foundUserWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(workspace.getId(), user.getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        // 모든 분위기 트래커 조회
        List<MoodTracker> foundMoodTrackers = moodTrackerPort.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());

        // 권한에 따른 필터링
        List<MoodTracker> accessibleMoodTrackers = foundMoodTrackers.stream()
                .filter(moodTracker ->
                        // 워크스페이스 생성자인 경우 모두 허용
                        foundUserWorkspace.getAuth().equals(Auth.ADMIN)

                        // 또는 해당 설문 생성자인 경우 허용
                        || moodTracker.getCreator().getId().equals(user.getId())

                        // 또는 공개된 설문인 경우 허용
                        || moodTracker.getVisibility().equals(MoodTrackerVisibility.PUBLIC)
                )
                .collect(Collectors.toList());

        return MoodTrackerConverter.toPreviewListDTO(accessibleMoodTrackers);
    }

    @Override
    @Transactional(readOnly = true)
    public MoodTrackerResponseDTO.BaseResult getBaseResult(Long moodTrackerId) {
        MoodTracker foundMoodTracker = moodTrackerPort.findById(moodTrackerId)
                .orElseThrow(() -> new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FOUND));

        return MoodTrackerConverter.toBaseResultDTO(foundMoodTracker);
    }

    @Override
    @Transactional(readOnly = true)
    public MoodTrackerResponseDTO.QuestionResult getQuestionResult(Long moodTrackerId) {
        MoodTracker foundMoodTracker = moodTrackerPort.findById(moodTrackerId)
                .orElseThrow(() -> new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FOUND));

        List<SurveyQuestion> questionList = surveyQuestionJpaRepository.findAllByMoodTrackerId(foundMoodTracker.getId());

        return MoodTrackerConverter.toQuestionResultDTO(foundMoodTracker, questionList);
    }

    @Override
    @Transactional
    @TrackLastOpened
    public MoodTrackerResponseDTO.ReportResult getReportResult(User user, MoodTracker moodTracker) {

        // 권한 확인
        UserWorkspace userWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(
                moodTracker.getWorkspace().getId(), user.getId()
        ).orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        boolean hasAccess =
                userWorkspace.getAuth().equals(Auth.ADMIN) ||
                        moodTracker.getCreator().getId().equals(user.getId()) ||
                        moodTracker.getVisibility().equals(MoodTrackerVisibility.PUBLIC);

        if (!hasAccess) {
            throw new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_ACCESS_DENIED);
        }

        // 마감 여부 확인
        if (LocalDateTime.now().isBefore(moodTracker.getDueDate())) {
            throw new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FINISHED);
        }

        List<String> suggestionList = surveyQuestionJpaRepository.findAllByMoodTrackerId(moodTracker.getId()).stream()
                .map(SurveyQuestion::getSuggestion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return MoodTrackerConverter.toReportResultDTO(moodTracker, suggestionList);
    }

    @Override
    @Transactional
    @TrackLastOpened
    public MoodTrackerResponseDTO.ResponseResult getResponseResult(User user, MoodTracker moodTracker) {

        // 권한 확인
        UserWorkspace userWorkspace = userWorkspaceQueryUseCase.getUserWorkspace(
                moodTracker.getWorkspace().getId(), user.getId()
        ).orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        boolean hasAccess =
                userWorkspace.getAuth().equals(Auth.ADMIN) ||
                        moodTracker.getCreator().getId().equals(user.getId()) ||
                        moodTracker.getVisibility().equals(MoodTrackerVisibility.PUBLIC);

        if (!hasAccess) {
            throw new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_ACCESS_DENIED);
        }

        // 마감 여부 확인
        if (LocalDateTime.now().isBefore(moodTracker.getDueDate())) {
            throw new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FINISHED);
        }

        List<SurveyQuestion> questions = surveyQuestionJpaRepository.findAllByMoodTrackerId(moodTracker.getId());

        return MoodTrackerConverter.toResponseResultDTO(moodTracker, questions);
    }
}

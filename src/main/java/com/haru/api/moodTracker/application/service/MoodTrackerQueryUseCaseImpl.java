package com.haru.api.moodTracker.application.service;

import com.haru.api.moodTracker.application.port.in.MoodTrackerQueryUseCase;
import com.haru.api.moodTracker.infrastructure.MoodTrackerRepository;
import com.haru.api.moodTracker.infrastructure.SurveyQuestionRepository;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.moodTracker.application.converter.MoodTrackerConverter;
import com.haru.api.moodTracker.presentation.dto.MoodTrackerResponseDTO;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.moodTracker.domain.SurveyQuestion;
import com.haru.api.moodTracker.domain.enums.MoodTrackerVisibility;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.UserWorkspace;
import com.haru.api.workspace.domain.enums.Auth;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MoodTrackerHandler;
import com.haru.api.global.apiPayload.exception.handler.UserWorkspaceHandler;
import com.haru.api.global.util.HashIdUtil;
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
    private final MoodTrackerRepository moodTrackerRepository;

    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;

    private final HashIdUtil hashIdUtil;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    @Override
    public MoodTrackerResponseDTO.PreviewList getPreviewList(User user, Workspace workspace) {

        UserWorkspace foundUserWorkspace = userWorkspaceJpaRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .orElseThrow(() -> new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND));

        // 모든 분위기 트래커 조회
        List<MoodTracker> foundMoodTrackers = moodTrackerRepository.findAllByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());

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

        MoodTrackerResponseDTO.PreviewList previewList = MoodTrackerConverter.toPreviewListDTO(accessibleMoodTrackers, hashIdUtil);
        return previewList;
    }

    @Override
    @Transactional(readOnly = true)
    public MoodTrackerResponseDTO.BaseResult getBaseResult(Long moodTrackerId) {
        MoodTracker foundMoodTracker = moodTrackerRepository.findById(moodTrackerId)
                .orElseThrow(() -> new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FOUND));

        return MoodTrackerConverter.toBaseResultDTO(foundMoodTracker, hashIdUtil);
    }

    @Override
    @Transactional(readOnly = true)
    public MoodTrackerResponseDTO.QuestionResult getQuestionResult(Long moodTrackerId) {
        MoodTracker foundMoodTracker = moodTrackerRepository.findById(moodTrackerId)
                .orElseThrow(() -> new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FOUND));

        List<SurveyQuestion> questionList = surveyQuestionRepository.findAllByMoodTrackerId(foundMoodTracker.getId());

        return MoodTrackerConverter.toQuestionResultDTO(foundMoodTracker, questionList, hashIdUtil);
    }

    @Override
    @Transactional
    public MoodTrackerResponseDTO.ReportResult getReportResult(User user, MoodTracker moodTracker) {

        // 최근 문서 조회 동기화
        Long workspaceId = moodTracker.getWorkspaceId();
        String title = moodTracker.getTitle();

        UserDocumentId userDocumentId = new UserDocumentId(user.getId(), moodTracker.getId(), moodTracker.getDocumentType());

        userDocumentLastOpenedQueryUseCase.updateLastOpened(userDocumentId, workspaceId, title);

        // 권한 확인
        UserWorkspace userWorkspace = userWorkspaceJpaRepository.findByWorkspaceIdAndUserId(
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

        List<String> suggestionList = surveyQuestionRepository.findAllByMoodTrackerId(moodTracker.getId()).stream()
                .map(SurveyQuestion::getSuggestion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return MoodTrackerConverter.toReportResultDTO(moodTracker, suggestionList, hashIdUtil);
    }

    @Override
    @Transactional
    public MoodTrackerResponseDTO.ResponseResult getResponseResult(User user, MoodTracker moodTracker) {

        // 최근 문서 조회 동기화
        Long workspaceId = moodTracker.getWorkspaceId();
        String title = moodTracker.getTitle();

        UserDocumentId userDocumentId = new UserDocumentId(user.getId(), moodTracker.getId(), moodTracker.getDocumentType());

        userDocumentLastOpenedQueryUseCase.updateLastOpened(userDocumentId, workspaceId, title);

        // 권한 확인
        UserWorkspace userWorkspace = userWorkspaceJpaRepository.findByWorkspaceIdAndUserId(
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

        List<SurveyQuestion> questions = surveyQuestionRepository.findAllByMoodTrackerId(moodTracker.getId());

        return MoodTrackerConverter.toResponseResultDTO(moodTracker, questions, hashIdUtil);
    }
}

package com.haru.api.moodTracker.presentation;

import com.haru.api.moodTracker.presentation.dto.MoodTrackerRequestDTO;
import com.haru.api.moodTracker.presentation.dto.MoodTrackerResponseDTO;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.moodTracker.application.port.in.MoodTrackerCommandUseCase;
import com.haru.api.moodTracker.application.port.in.MoodTrackerQueryUseCase;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.global.annotation.AuthMoodTracker;
import com.haru.api.global.annotation.AuthUser;
import com.haru.api.global.annotation.AuthWorkspace;
import com.haru.api.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import com.haru.api.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/mood-trackers")
@RequiredArgsConstructor
public class MoodTrackerController {

    private final MoodTrackerCommandUseCase moodTrackerCommandUseCase;

    private final MoodTrackerQueryUseCase moodTrackerQueryUseCase;


    @GetMapping("/workspaces/{workspaceId}")
    @Operation(
            summary = "워크스페이스별 분위기 트래커 리스트 조회 API",
            description = "# [v1.1 (2025-08-05)](https://www.notion.so/2265da7802c580048e63f104d98d7637) 해당 워크스페이스(workspaceId)에 소속된 분위기 트래커 설문들을 모두 조회합니다."
    )
    @Parameters({
            @Parameter(name = "workspaceId", description = "워크스페이스 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.PreviewList> getMoodTrackerPreviewListByWorkspace(
            @PathVariable String workspaceId,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthWorkspace Workspace workspace
    ) {

        MoodTrackerResponseDTO.PreviewList result = moodTrackerQueryUseCase.getPreviewList(user, workspace);

        return ApiResponse.onSuccess(result);

    }

    @PostMapping("/workspaces/{workspaceId}")
    @Operation(
            summary = "분위기 트래커 설문 생성 API",
            description = "# [v1.1 (2025-08-05)](https://www.notion.so/2265da7802c580429bd5ed5067cbe5ba) 워크스페이스 ID, 설문 제목, 마감일 등을 입력받아 새로운 분위기 트래커 설문을 생성합니다."
    )
    @Parameters({
            @Parameter(name = "workspaceId", description = "워크스페이스 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.CreateResult> createMoodTracker(
            @PathVariable String workspaceId,
            @RequestBody @Valid MoodTrackerRequestDTO.CreateRequest request,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthWorkspace Workspace workspace
    ) {

        MoodTrackerResponseDTO.CreateResult result = moodTrackerCommandUseCase.create(user, workspace,request);

        return ApiResponse.of(SuccessStatus.MOOD_TRACKER_CREATED, result);

    }

    @PatchMapping("/{moodTrackerId}")
    @Operation(
            summary = "분위기 트래커 설문명 수정 API",
            description = "# [v1.0 (2025-07-26)](https://www.notion.so/22a5da7802c580fe80ece5981e90b03b) 해당 ID의 분위기 트래커 설문 제목(title)을 수정합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<Void> updateMoodTrackerTitle(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @RequestBody @Valid MoodTrackerRequestDTO.UpdateTitleRequest request,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        moodTrackerCommandUseCase.updateTitle(user, moodTracker, request);

        return ApiResponse.of(SuccessStatus.MOOD_TRACKER_UPDATED, null);

    }

    @DeleteMapping("/{moodTrackerId}")
    @Operation(
            summary = "분위기 트래커 설문 삭제 API",
            description = "# [v1.0 (2025-07-26)](https://www.notion.so/2265da7802c58011aa54ea2c1818ef04) 해당 ID의 분위기 트래커 설문을 삭제합니다."
    )
    @Parameters({
            @Parameter(name = "mood-tracker-hashed-Id", description = "해시된 16자 분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<Void> deleteMoodTracker(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        moodTrackerCommandUseCase.delete(user, moodTracker);

        return ApiResponse.of(SuccessStatus.MOOD_TRACKER_DELETED, null);

    }

    @PostMapping("/{moodTrackerId}/emails")
    @Operation(
            summary = "분위기 트래커 설문 링크 워크 스페이스 내의 팀원 email 전송 API",
            description = "# [v1.0 (2025-07-26)](https://www.notion.so/22a5da7802c580a799cec13c005824d7) 해당 ID의 분위기 트래커 설문 링크를 워크 스페이스 내의 유저에게 email로 전송합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "해시된 16자 분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<Void> sendMoodTrackerSurveyLink(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        moodTrackerCommandUseCase.sendSurveyLink(moodTracker);

        return ApiResponse.of(SuccessStatus.MOOD_TRACKER_EMAIL_SENT, null);

    }

    @PostMapping("/{moodTrackerId}/answer")
    @Operation(
            summary = "분위기 트래커 설문 답변 제출 API (비인증)",
            description = "# [v1.2 (2025-08-19)](https://www.notion.so/2265da7802c580c58d36e73639e41291) 해당 ID의 분위기 트래커 설문 답변을 제출합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public  ApiResponse<Void> submitMoodTrackerSurveyAnswers(
            @PathVariable("moodTrackerId") Long moodTrackerId,
            @Valid @RequestBody MoodTrackerRequestDTO.SurveyAnswerList request
    ) {

        moodTrackerCommandUseCase.submitSurveyAnswers(moodTrackerId, request);

        return ApiResponse.of(SuccessStatus.MOOD_TRACKER_ANSWER_SUBMIT, null);

    }

    @GetMapping("/{moodTrackerId}/bases")
    @Operation(
            summary = "분위기 트래커 설문 팀분위기 베이스 정보 조회 API (비인증)",
            description = "# [v1.1 (2025-08-19)](https://www.notion.so/2545da7802c580dd9742d971d3a4bc08?source=copy_link) 분위기 트래커(moodTrackerId)에 대한 베이스 정보를 조회합니다."
    )
    @Parameters({
            @Parameter(name = "mood-tracker-hashed-Id", description = "분위기 트래커 ID (Hashed, Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.BaseResult> getMoodTrackerBaseResult(
            @PathVariable(name = "moodTrackerId") Long moodTrackerId
    ) {

        MoodTrackerResponseDTO.BaseResult result = moodTrackerQueryUseCase.getBaseResult(moodTrackerId);

        return ApiResponse.onSuccess(result);

    }

    @GetMapping("/{moodTrackerId}/questions")
    @Operation(
            summary = "분위기 트래커 설문 문항 조회 API (비인증)",
            description = "# [v1.3 (2025-08-19)](https://www.notion.so/2295da7802c580dbb88aee8687b69e32) 분위기 트래커(moodTrackerId)에 해당하는 설문 문항들을 조회합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.QuestionResult> getMoodTrackerQuestionResult(
            @PathVariable(name = "moodTrackerId") Long moodTrackerId
    ) {

        MoodTrackerResponseDTO.QuestionResult result = moodTrackerQueryUseCase.getQuestionResult(moodTrackerId);

        return ApiResponse.onSuccess(result);

    }

    @GetMapping("/{moodTrackerId}/reports")
    @Operation(
            summary = "분위기 트래커 설문 팀분위기 리포트 조회 API + last opend 처리",
            description = "# [v1.2 (2025-08-05)](https://www.notion.so/2295da7802c580ba8401c449389e8f78) 분위기 트래커(moodTrackerId)에 대한 팀 전체 리포트를 조회합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.ReportResult> getMoodTrackerReportResult(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        MoodTrackerResponseDTO.ReportResult result = moodTrackerQueryUseCase.getReportResult(user, moodTracker);

        return ApiResponse.onSuccess(result);

    }

    @GetMapping("/{moodTrackerId}/responses")
    @Operation(
            summary = "분위기 트래커 설문 응답 조회 API + last opened 처리",
            description = "# [v1.2 (2025-08-05)](https://www.notion.so/2265da7802c5808290adf17d8d4591a4) 분위기 트래커(moodTrackerId)에 대한 개별 응답 데이터를 조회합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.ResponseResult> getMoodTrackerResponseResult(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        MoodTrackerResponseDTO.ResponseResult result = moodTrackerQueryUseCase.getResponseResult(user, moodTracker);

        return ApiResponse.onSuccess(result);

    }

    @GetMapping("/{moodTrackerId}/download")
    @Operation(
            summary = "분위기 트래커 설문 리포트 다운로드 API",
            description = "# [v1.0 (2025-08-13)](https://www.notion.so/2265da7802c580e3b53ddd8d181922b1) 분위기 트래커(moodTrackerId)에 대한 개별 리포트 다운로드 링크를 조회합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public ApiResponse<MoodTrackerResponseDTO.ReportDownLoadLinkResponse> downloadList(
            @PathVariable(name = "moodTrackerId") String moodTrackerId,
            @RequestParam Format format,
            @Parameter(hidden = true) @AuthUser User user,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        MoodTrackerResponseDTO.ReportDownLoadLinkResponse result = moodTrackerCommandUseCase.getDownloadLink(user, moodTracker, format);

        return ApiResponse.onSuccess(result);

    }

    @PostMapping("/{moodTrackerId}/report-test")
    @Operation(
            summary = "분위기 트래커 설문 리포트 즉시 생성 API",
            description = "# [v1.0 (2025-07-26)](https://www.notion.so/23f5da7802c58080b4a5e6d24b47d924) 해당 ID의 분위기 트래커 설문 리포트를 즉시 생성합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "16자 분위기 트래커 ID (Path Variable)", required = true)
    })
    public  ApiResponse<Void> generateMoodTrackerReportTest (
            @PathVariable("moodTrackerId") String moodTrackerId,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        moodTrackerCommandUseCase.generateReportTest(moodTracker);

        return ApiResponse.of(SuccessStatus._OK, null);

    }

    @PostMapping("/{moodTrackerId}/report-file-thumbnail-test")
    @Operation(
            summary = "분위기 트래커 설문 리포트, 파일, 썸네일 즉시 생성 테스트 API + redis에서 제외하여 마감일시에 중복 생성 불가",
            description = "# [v1.0 (2025-08-14)](https://www.notion.so/24f5da7802c58019a1f7d9c8e882226e) 해당 ID의 분위기 트래커 설문 리포트를 즉시 생성합니다."
    )
    @Parameters({
            @Parameter(name = "moodTrackerId", description = "분위기 트래커 ID (Path Variable)", required = true)
    })
    public  ApiResponse<Void> generateMoodTrackerReportFileAndThumbnailTest (
            @PathVariable("moodTrackerId") Long moodTrackerId,
            @Parameter(hidden = true) @AuthMoodTracker MoodTracker moodTracker
    ) {

        moodTrackerCommandUseCase.generateReportFileAndThumbnailTest(moodTracker);

        return ApiResponse.of(SuccessStatus._OK, null);

    }
}

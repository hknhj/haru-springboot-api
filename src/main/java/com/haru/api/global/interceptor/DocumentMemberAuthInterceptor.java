package com.haru.api.global.interceptor;

import com.haru.api.meeting.application.port.in.MeetingQueryUseCase;
import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.moodTracker.infrastructure.MoodTrackerRepository;
import com.haru.api.snsEvent.infrastructure.SnsEventRepository;
import com.haru.api.user.domain.User;
import com.haru.api.infra.security.jwt.SecurityUtil;
import com.haru.api.global.annotation.AuthDocument;
import com.haru.api.global.annotation.AuthUser;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.global.apiPayload.exception.handler.MoodTrackerHandler;
import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.global.util.HashIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.annotation.Annotation;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocumentMemberAuthInterceptor implements HandlerInterceptor {

    private final UserPort userPort;

    private final HashIdUtil hashIdUtil;

    private final MeetingQueryUseCase meetingQueryUseCase;
    private final SnsEventRepository snsEventRepository;
    private final MoodTrackerRepository moodTrackerRepository;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {

        // 컨트롤러 메서드인지 확인
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        final HandlerMethod handlerMethod = (HandlerMethod) handler;

        // @AuthUser, @AuthDocument 어노테이션이 달린 인자가 있는지 확인
        boolean hasAuthUserParam = false;
        AuthDocument authDocumentInfo = null;

        for (final var param : handlerMethod.getMethodParameters()) {
            if (param.hasParameterAnnotation(AuthUser.class)) {
                hasAuthUserParam = true;
            }

            // 파라미터에 붙은 모든 어노테이션을 순회
            for (final Annotation annotation : param.getParameterAnnotations()) {
                // 해당 어노테이션의 타입에 @AuthDocument 메타 어노테이션이 있는지 확인
                if (annotation.annotationType().isAnnotationPresent(AuthDocument.class)) {
                    // @AuthDocument 어노테이션의 실제 인스턴스를 가져와 저장
                    authDocumentInfo = annotation.annotationType().getAnnotation(AuthDocument.class);
                    break;
                }
            }
        }

        // AuthUser, AuthDocument가 모두 존재하는 경우
        if (hasAuthUserParam && authDocumentInfo != null) {

            // AuthDocument에서 DocumentType, pathVariableName 추출
            DocumentType documentType = authDocumentInfo.documentType();
            String pathVariableName = authDocumentInfo.pathVariableName();

            // URL pathvariable에서 documentId 추출
            final Map<String, String> pathVariables =
                    (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            final String documentIdStr = pathVariables.get(pathVariableName);

            if (documentIdStr == null) {
                throw new BadRequestException("경로 변수 " + pathVariableName + "가 없습니다.");
            }

            // userId
            final Long userId = SecurityUtil.getCurrentUserId();

            Object foundDocument = switch (documentType) {
                case AI_MEETING_MANAGER -> {
                    Long documentId = Long.parseLong(documentIdStr);
                    yield meetingQueryUseCase.getDocumentWithPermissionCheck(userId, documentId)
                            .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_HAS_NO_ACCESS_TO_MEETING));
                }
                case SNS_EVENT_ASSISTANT -> {
                    Long documentId = Long.parseLong(documentIdStr);
                    yield snsEventRepository.findSnsEventByIdIfUserHasAccess(userId, documentId)
                            .orElseThrow(() -> new SnsEventHandler(ErrorStatus.SNS_EVENT_NOT_FOUND));
                }
                case TEAM_MOOD_TRACKER -> {
                    Long documentId = hashIdUtil.decode(documentIdStr);
                    yield moodTrackerRepository.findMoodTrackerByIdIfUserHasAccess(userId, documentId)
                            .orElseThrow(() -> new MoodTrackerHandler(ErrorStatus.MOOD_TRACKER_NOT_FOUND));
                }
            };

            // 유저 조회
            User foundUser = userPort.findById(userId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

            // request에 attribute 저장
            request.setAttribute("isValidated", true);
            request.setAttribute("validatedUser", foundUser);
            request.setAttribute("validatedDocument", foundDocument);
        }

        return true;
    }

}

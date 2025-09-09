package com.haru.api.global.interceptor;

import com.haru.api.user.application.port.out.UserPort;
import com.haru.api.user.domain.User;
import com.haru.api.infra.security.jwt.SecurityUtil;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.workspace.infrastructure.jpa.WorkspaceJpaRepository;
import com.haru.api.global.annotation.AuthUser;
import com.haru.api.global.annotation.AuthWorkspace;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MemberHandler;
import com.haru.api.global.apiPayload.exception.handler.UserWorkspaceHandler;
import com.haru.api.global.apiPayload.exception.handler.WorkspaceHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WorkspaceMemberAuthInterceptor implements HandlerInterceptor {

    private final UserPort userPort;

    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;

    private final WorkspaceJpaRepository workspaceJpaRepository;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {

        // 컨트롤러 메서드인지 확인
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        final HandlerMethod handlerMethod = (HandlerMethod) handler;

        // @AuthUser, @AuthWorkspace 어노테이션이 달린 인자가 있는지 확인
        boolean hasAuthUserParam = false;
        boolean hasAuthWorkspaceParam = false;

        for (final var param : handlerMethod.getMethodParameters()) {
            if (param.hasParameterAnnotation(AuthUser.class)) {
                hasAuthUserParam = true;
            }
            if (param.hasParameterAnnotation(AuthWorkspace.class)) {
                hasAuthWorkspaceParam = true;
            }
        }

        // 컨트롤러에 AuthUser, AuthWorkspace 어노테이션이 모두 달린 경우에 해당
        if(hasAuthUserParam && hasAuthWorkspaceParam) {

            // URL pathVariable에서 workspaceId 추출
            final Map<String, String> pathVariables =
                    (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            final String workspaceId = pathVariables.get("workspaceId");

            if (workspaceId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Workspace ID is missing from path.");
                return false;
            }

            // Security Context에서 현재 유저 ID 추출
            final Long userId = SecurityUtil.getCurrentUserId();

            // 유저 조회
            User foundUser = userPort.findUserById(userId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

            // 워크스페이스 조회
            Workspace foundWorkspace = workspaceJpaRepository.findById(Long.parseLong(workspaceId))
                    .orElseThrow(() -> new WorkspaceHandler(ErrorStatus.WORKSPACE_NOT_FOUND));

            // 유저가 워크스페이스에 속하는지 확인
            final boolean isUserInWorkspace = userWorkspaceJpaRepository.existsByUserIdAndWorkspaceId(foundUser.getId(), foundWorkspace.getId());

            // 속하지 않는 경우 예외 발생
            if(!isUserInWorkspace) throw new UserWorkspaceHandler(ErrorStatus.USER_WORKSPACE_NOT_FOUND);

            request.setAttribute("isValidated", true);
            request.setAttribute("validatedUser", foundUser);
            request.setAttribute("validatedWorkspace", foundWorkspace);
        }

        return true;
    }
}

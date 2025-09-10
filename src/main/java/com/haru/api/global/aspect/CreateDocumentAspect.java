package com.haru.api.global.aspect;

import com.haru.api.global.annotation.CreateDocument;
import com.haru.api.shared_kernel.domain.CreatedDocument;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.user.domain.enums.DocumentType;
import com.haru.api.workspace.application.port.in.UserWorkspaceQueryUseCase;
import com.haru.api.workspace.domain.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CreateDocumentAspect {

    private final UserDocumentLastOpenedCommandUseCase userDocumentLastOpenedCommandUseCase;
    private final UserWorkspaceQueryUseCase userWorkspaceQueryUseCase;

    @AfterReturning(
            pointcut = "@annotation(createDocument)",
            returning = "result"
    )
    public void afterCreateDocument(JoinPoint joinPoint, Object result, CreateDocument createDocument) {

        Workspace workspace = null;
        DocumentType documentType = createDocument.documentType();

        // 실행된 메서드의 인자 추출
        Object[] args = joinPoint.getArgs();

        for(Object arg : args) {
            if (arg instanceof Workspace) {
                workspace = (Workspace) arg;
            }
        }

        if (result instanceof CreatedDocument) {
            List<User> usersInWorkspace = userWorkspaceQueryUseCase.getWorkspaceMembers(workspace.getId());
            userDocumentLastOpenedCommandUseCase.createInitialRecordsForWorkspaceUsers(usersInWorkspace, (CreatedDocument) result, workspace.getId(), documentType);
        }

        log.info("user document last opened created");
    }
}

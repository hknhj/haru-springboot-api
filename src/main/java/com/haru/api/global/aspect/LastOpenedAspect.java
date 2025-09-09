package com.haru.api.global.aspect;

import com.haru.api.global.common.Documentable;
import com.haru.api.user.domain.UserDocumentId;
import com.haru.api.workspace.application.port.in.UserDocumentLastOpenedQueryUseCase;
import com.haru.api.user.domain.User;
import com.haru.api.global.annotation.TrackLastOpened;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Order(1)
public class LastOpenedAspect {

    private final UserDocumentLastOpenedQueryUseCase userDocumentLastOpenedQueryUseCase;

    @Around("@annotation(trackLastOpened)")
    public Object trackLastOpened(ProceedingJoinPoint joinPoint, TrackLastOpened trackLastOpened) throws Throwable {
        // 실제 메서드 실행
        Object result = joinPoint.proceed();

        // 메서드의 인자에서 user와 document 추출
        Object[] args = joinPoint.getArgs();

        // 인덱스를 사용하여 user와 document 추출
        User user = (User)args[0];
        Documentable document = (Documentable)args[1];

        if (user != null && document != null) {

            Long workspaceId = document.getWorkspaceId();
            String title = document.getTitle();

            UserDocumentId userDocumentId = new UserDocumentId(user.getId(), document.getId(), document.getDocumentType());

            userDocumentLastOpenedQueryUseCase.updateLastOpened(userDocumentId, workspaceId, title);
        }

        return result;
    }

}

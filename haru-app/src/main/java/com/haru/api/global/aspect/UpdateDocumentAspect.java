package com.haru.api.global.aspect;

import com.haru.api.common_library.domain.Documentable;
import com.haru.api.user.application.port.in.UserDocumentLastOpenedCommandUseCase;
import com.haru.api.common_library.domain.DocumentModifier;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class UpdateDocumentAspect {

    private final UserDocumentLastOpenedCommandUseCase userDocumentLastOpenedCommandUseCase;

    @AfterReturning("@annotation(com.haru.api.global.annotation.UpdateDocument)")
    public void afterUpdateDocument(JoinPoint joinPoint) {

        Documentable document = null;
        DocumentModifier documentModifier = null;

        // 실행된 메서드의 인자 추출
        Object[] args = joinPoint.getArgs();

        // 인자들 중에서 Documentable 타입의 객체 추출
        for (Object arg : args) {
            if (arg instanceof Documentable) {
                document = (Documentable) arg;
            } else if (arg instanceof DocumentModifier) {
                documentModifier = (DocumentModifier) arg;
            }
        }

        // document, titleHolder가 정상적으로 조회되면 last opened 테이블에서 해당 문서의 제목 수정
        if (document != null && documentModifier != null) {
            userDocumentLastOpenedCommandUseCase.updateRecordsTitleAndThumbnailForWorkspaceUsers(document, documentModifier);
        }
    }
}

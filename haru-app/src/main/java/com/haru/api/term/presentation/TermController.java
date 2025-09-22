package com.haru.api.term.presentation;

import com.haru.api.common_library.dto.term.TermResponseDTO;
import com.haru.api.term.domain.enums.TermType;
import com.haru.api.term.application.port.in.TermUseCase;
import com.haru.api.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController {
    private final TermUseCase termUseCase;

    @GetMapping
    @Operation(
            summary = "약관 조회 API",
            description = "# [v1.0 (2025-08-05)](https://www.notion.so//2465da7802c58010b89de43e6ffb77a2) type에 해당하는 약관을 조회합니다.\n\n" +
                    "- SERVICE: 서비스 이용 약관\n" +
                    "- PRIVACY: 개인정보 처리방침\n" +
                    "- MARKETING: 마케팅 정보 수신"
    )
    public ApiResponse<TermResponseDTO.TermDetail> getTermsByType(
            @Parameter(
                    name = "type",
                    description = "조회할 약관의 종류\n\n- SERVICE: 서비스 이용 약관\n- PRIVACY: 개인정보 처리방침\n- MARKETING: 마케팅 정보 수신",
                    required = true,
                    in = ParameterIn.QUERY,
                    schema = @Schema(implementation = TermType.class)
            )
            @RequestParam TermType type
    ) {
        return ApiResponse.onSuccess(termUseCase.getTermByType(type));
    }
}

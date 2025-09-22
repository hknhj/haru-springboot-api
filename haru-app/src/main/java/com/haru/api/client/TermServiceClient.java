package com.haru.api.client;

import com.haru.api.global.apiPayload.ApiResponse;
import com.haru.api.term.domain.enums.TermType;
import com.haru.api.common_library.dto.term.TermResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "term-service", path = "/api/v1/terms")
public interface TermServiceClient {

    @GetMapping
    ApiResponse<TermResponseDTO.TermDetail> getTermsByType(@RequestParam("type") TermType type);
}

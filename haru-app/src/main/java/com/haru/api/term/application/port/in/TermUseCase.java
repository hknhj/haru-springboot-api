package com.haru.api.term.application.port.in;

import com.haru.api.common_library.dto.term.TermResponseDTO;
import com.haru.api.term.domain.enums.TermType;

public interface TermUseCase {
    TermResponseDTO.TermDetail getTermByType(TermType type);
}

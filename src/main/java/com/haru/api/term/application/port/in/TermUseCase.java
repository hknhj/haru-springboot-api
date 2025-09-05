package com.haru.api.term.application.port.in;

import com.haru.api.term.presentation.dto.TermResponseDTO;
import com.haru.api.term.domain.enums.TermType;

public interface TermUseCase {
    TermResponseDTO.TermDetail getTermByType(TermType type);
}

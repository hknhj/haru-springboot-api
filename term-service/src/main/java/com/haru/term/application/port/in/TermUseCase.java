package com.haru.term.application.port.in;

import com.haru.common.dto.term.TermResponseDTO;
import com.haru.common.type.TermType;

public interface TermUseCase {

    TermResponseDTO.TermDetail getTermByType(TermType type);

}

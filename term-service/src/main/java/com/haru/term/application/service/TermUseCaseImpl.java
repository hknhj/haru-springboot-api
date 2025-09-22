package com.haru.term.application.service;

import com.haru.term.application.TermConverter;
import com.haru.term.application.port.in.TermUseCase;
import com.haru.term.application.port.out.TermPort;
import com.haru.term.domain.Term;
import com.haru.common.apiPayload.code.status.ErrorStatus;
import com.haru.common.apiPayload.exception.GeneralException;
import com.haru.common.dto.term.TermResponseDTO;
import com.haru.common.type.TermType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TermUseCaseImpl implements TermUseCase {

    private final TermPort termPort;

    @Override
    public TermResponseDTO.TermDetail getTermByType(TermType type) {
        Term term = termPort.findByType(type)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TERM_NOT_FOUND));
        return TermConverter.toTermsDetailDTO(term);
    }
}

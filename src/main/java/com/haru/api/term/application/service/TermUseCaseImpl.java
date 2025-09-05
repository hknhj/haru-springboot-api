package com.haru.api.term.application.service;

import com.haru.api.term.application.converter.TermConverter;
import com.haru.api.term.application.port.in.TermUseCase;
import com.haru.api.term.presentation.dto.TermResponseDTO;
import com.haru.api.term.domain.Term;
import com.haru.api.term.domain.enums.TermType;
import com.haru.api.term.infrastructure.TermRepository;
import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermUseCaseImpl implements TermUseCase {

    private final TermRepository termsRepository;

    @Override
    @Transactional(readOnly = true)
    public TermResponseDTO.TermDetail getTermByType(TermType type) {
        Term term = termsRepository.findByType(type)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TERM_NOT_FOUND));
        return TermConverter.toTermsDetailDTO(term);
    }
}

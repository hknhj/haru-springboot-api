package com.haru.api.term.infrastructure.adapter;

import com.haru.api.term.application.port.out.TermPort;
import com.haru.api.term.domain.Term;
import com.haru.api.term.domain.enums.TermType;
import com.haru.api.term.infrastructure.jpa.TermJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TermPersistenceAdapter implements TermPort {

    private final TermJpaRepository termJpaRepository;

    @Override
    public Optional<Term> findByType(TermType type) {
        return termJpaRepository.findByType(type);
    }
}

package com.haru.term.infrastructure.adapter;


import com.haru.term.application.port.out.TermPort;
import com.haru.term.domain.Term;
import com.haru.term.infrastructure.jpa.TermJpaRepository;
import com.haru.common.type.TermType;
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

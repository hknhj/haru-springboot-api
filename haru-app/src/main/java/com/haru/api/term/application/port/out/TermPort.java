package com.haru.api.term.application.port.out;

import com.haru.api.term.domain.Term;
import com.haru.api.term.domain.enums.TermType;

import java.util.Optional;

public interface TermPort {

    Optional<Term> findByType(TermType type);

}

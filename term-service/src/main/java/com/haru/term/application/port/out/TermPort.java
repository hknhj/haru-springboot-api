package com.haru.term.application.port.out;

import com.haru.term.domain.Term;
import com.haru.common.type.TermType;

import java.util.Optional;

public interface TermPort {

    Optional<Term> findByType(TermType type);

}

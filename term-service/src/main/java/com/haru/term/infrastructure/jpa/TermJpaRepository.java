package com.haru.term.infrastructure.jpa;

import com.haru.term.domain.Term;
import com.haru.common.type.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermJpaRepository extends JpaRepository<Term, Long> {
    Optional<Term> findByType(TermType type);
}

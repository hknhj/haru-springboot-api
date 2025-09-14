package com.haru.api.term.infrastructure.jpa;

import com.haru.api.term.domain.Term;
import com.haru.api.term.domain.enums.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermJpaRepository extends JpaRepository<Term, Long> {
    Optional<Term> findByType(TermType type);
}

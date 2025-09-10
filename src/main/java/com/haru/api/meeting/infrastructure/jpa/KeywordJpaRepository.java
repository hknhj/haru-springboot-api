package com.haru.api.meeting.infrastructure.jpa;

import com.haru.api.meeting.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordJpaRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByName(String name);
}

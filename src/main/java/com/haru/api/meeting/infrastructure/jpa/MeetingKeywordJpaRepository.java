package com.haru.api.meeting.infrastructure.jpa;

import com.haru.api.meeting.domain.MeetingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingKeywordJpaRepository extends JpaRepository<MeetingKeyword, Long> {
}

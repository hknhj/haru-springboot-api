package com.haru.api.meeting.infrastructure;

import com.haru.api.meeting.domain.MeetingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingKeywordRepository extends JpaRepository<MeetingKeyword, Long> {
}

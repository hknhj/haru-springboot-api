package com.haru.api.snsEvent.infrastructure.jpa;

import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WinnerJpaRepository extends JpaRepository<Winner, Long> {

    List<Winner> findAllBySnsEvent(SnsEvent foundSnsEvent);

}

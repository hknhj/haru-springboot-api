package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.Winner;

import java.util.List;

public interface WinnerPort {

    void saveAll(List<Winner> winners);

    List<Winner> findAllBySnsEventId(Long snsEventId);

}

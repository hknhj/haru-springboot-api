package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;

import java.util.List;

public interface WinnerPort {

    List<Winner> findAllBySnsEvent(SnsEvent foundSnsEvent);

}

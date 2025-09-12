package com.haru.api.snsEvent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PickWinner {

    public List<String> getWinners(Set<String> participants, int n) {
        List<String> list = new ArrayList<>(participants); // Set → List로 변환
        Collections.shuffle(list); // 무작위 섞기

        if (n >= list.size()) {
            return list; // 참가자가 n보다 적으면 전원 반환
        }

        return list.subList(0, n); // 앞에서 n개만 추출
    }
}

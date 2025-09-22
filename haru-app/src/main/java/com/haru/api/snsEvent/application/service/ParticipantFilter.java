package com.haru.api.snsEvent.application.service;

import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ParticipantFilter {

    public Set<String> getFilteredParticipant(
            List<SnsEventResponseDTO.Comment> commentList,
            SnsEventRequestDTO.SnsCondition condition)
    {

        List<Participant> filteredParticipants = new ArrayList<>();
        Set<String> filteredCommentSet = new HashSet<>();

        for (SnsEventResponseDTO.Comment comment : commentList) {
            boolean pass = true;
            // 조건 1: 기간 필터
            LocalDateTime commentTimeStamp = comment.getTimestamp().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
            if (condition.getIsPeriod()) {
                if (comment.getTimestamp() == null || commentTimeStamp.isAfter(condition.getPeriod())) {
                    pass = false;
                }
            }
            // 조건 2: 키워드 필터
            if (pass && condition.getIsKeyword()) {
                Boolean hasKeyword = false;
                String[] keywords = condition.getKeyword().trim().split(" ");
                for (String keyword : keywords) {
                    System.out.println("키워드: " + keyword);
                    if (comment.getText() == null || !comment.getText().contains(keyword)) {
                        hasKeyword = true;
                    }
                }
                if (!hasKeyword) {
                    pass = false;
                }
            }
            // 조건 3: 태그 개수 필터 (ex: @username 언급)
            if (pass && condition.getIsTagged()) {
                int tagCount = countOccurrences(comment.getText(), "@");
                if (tagCount < condition.getTagCount()) {
                    pass = false;
                }
            }
            if (pass) {
                filteredCommentSet.add(comment.getFrom().getUsername()); // 중복 제거를 위해 Set 사용
            }
        }

        return filteredCommentSet;
    }

    private int countOccurrences(
            String text,
            String keyword
    ) {
        if (text == null || keyword == null || keyword.isEmpty()) return 0;

        int count = 0, idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }
}

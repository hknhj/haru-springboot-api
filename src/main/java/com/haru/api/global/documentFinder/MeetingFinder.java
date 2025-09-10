package com.haru.api.global.documentFinder;

import com.haru.api.meeting.application.port.in.MeetingQueryUseCase;
import com.haru.api.user.domain.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingFinder implements DocumentFinder{

    private final MeetingQueryUseCase meetingQueryUseCase;

    @Override
    public DocumentType getSupportType() {
        return DocumentType.AI_MEETING_MANAGER;
    }

    @Override
    public Object findById(Object id) {
        return meetingQueryUseCase.getMeeting((Long)id);
    }
}

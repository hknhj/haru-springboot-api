package com.haru.api.common_library.application.service;

import com.haru.api.meeting.application.port.in.MeetingQueryUseCase;
import com.haru.api.moodTracker.infrastructure.jpa.MoodTrackerJpaRepository;
import com.haru.api.common_library.application.port.in.DocumentQueryUseCase;
import com.haru.api.common_library.domain.Documentable;
import com.haru.api.snsEvent.infrastructure.jpa.SnsEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentQueryUseCaseImpl implements DocumentQueryUseCase {

    private final MeetingQueryUseCase meetingQueryUseCase;
    private final SnsEventJpaRepository snsEventJpaRepository;
    private final MoodTrackerJpaRepository moodTrackerJpaRepository;

    @Override
    public List<Documentable> getDocumentsByWorkspaceId(Long workspaceId) {

        List<Documentable> documentList = new ArrayList<>();

        documentList.addAll(meetingQueryUseCase.getAllMeetingsInWorkspace(workspaceId));
        documentList.addAll(snsEventJpaRepository.findAllByWorkspaceId(workspaceId));
        documentList.addAll(moodTrackerJpaRepository.findAllByWorkspaceId(workspaceId));

        return documentList;
    }

    @Override
    public List<Documentable> getAllDocumentsForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate) {

        List<Documentable> documentList = new ArrayList<>();

        documentList.addAll(meetingQueryUseCase.getAllMeetingsForCalendar(workspaceId, startDate, endDate));
        documentList.addAll(snsEventJpaRepository.findAllForCalendars(workspaceId, startDate, endDate));
        documentList.addAll(moodTrackerJpaRepository.findAllForCalendars(workspaceId, startDate, endDate));

        return documentList;
    }
}

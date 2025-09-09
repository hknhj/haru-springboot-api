package com.haru.api.shared_kernel.application.service;

import com.haru.api.meeting.infrastructure.MeetingRepository;
import com.haru.api.moodTracker.infrastructure.MoodTrackerRepository;
import com.haru.api.shared_kernel.application.port.in.DocumentQueryUseCase;
import com.haru.api.shared_kernel.domain.Documentable;
import com.haru.api.snsEvent.infrastructure.SnsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentQueryUseCaseImpl implements DocumentQueryUseCase {

    private final MeetingRepository meetingRepository;
    private final SnsEventRepository snsEventRepository;
    private final MoodTrackerRepository moodTrackerRepository;

    @Override
    public List<Documentable> getDocumentsByWorkspaceId(Long workspaceId) {

        List<Documentable> documentList = new ArrayList<>();

        documentList.addAll(meetingRepository.findAllByWorkspaceId(workspaceId));
        documentList.addAll(snsEventRepository.findAllByWorkspaceId(workspaceId));
        documentList.addAll(moodTrackerRepository.findAllByWorkspaceId(workspaceId));

        return documentList;
    }

    @Override
    public List<Documentable> getAllDocumentsForCalendars(Long workspaceId, LocalDateTime startDate, LocalDateTime endDate) {

        List<Documentable> documentList = new ArrayList<>();

        documentList.addAll(meetingRepository.findAllDocumentForCalendars(workspaceId, startDate, endDate));
        documentList.addAll(snsEventRepository.findAllDocumentForCalendars(workspaceId, startDate, endDate));
        documentList.addAll(moodTrackerRepository.findAllDocumentForCalendars(workspaceId, startDate, endDate));

        return documentList;
    }
}

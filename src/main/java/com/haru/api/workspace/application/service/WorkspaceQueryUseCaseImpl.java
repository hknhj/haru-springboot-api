package com.haru.api.workspace.application.service;

import com.haru.api.workspace.application.port.in.WorkspaceQueryUseCase;
import com.haru.api.workspace.domain.UserDocumentLastOpened;
import com.haru.api.workspace.infrastructure.jpa.UserDocumentLastOpenedJpaRepository;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.meeting.infrastructure.MeetingRepository;
import com.haru.api.moodTracker.domain.MoodTracker;
import com.haru.api.moodTracker.infrastructure.MoodTrackerRepository;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.infrastructure.SnsEventRepository;
import com.haru.api.user.application.converter.UserConverter;
import com.haru.api.user.presentation.dto.UserResponseDTO;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.infrastructure.jpa.UserWorkspaceJpaRepository;
import com.haru.api.workspace.application.converter.WorkspaceConverter;
import com.haru.api.workspace.presentation.dto.WorkspaceResponseDTO;
import com.haru.api.workspace.domain.Workspace;
import com.haru.api.infra.s3.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceQueryUseCaseImpl implements WorkspaceQueryUseCase {

    private final MeetingRepository meetingRepository;
    private final SnsEventRepository snsEventRepository;
    private final MoodTrackerRepository moodTrackerRepository;
    private final UserWorkspaceJpaRepository userWorkspaceJpaRepository;
    private final UserDocumentLastOpenedJpaRepository userDocumentLastOpenedJpaRepository;
    private final WorkspaceConverter workspaceConverter;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    public WorkspaceResponseDTO.DocumentList getDocuments(User user, Workspace workspace, String title) {

        List<UserDocumentLastOpened> documentList = userDocumentLastOpenedJpaRepository.findRecentDocumentsByTitle(user.getId(), workspace.getId(), title);

        return WorkspaceConverter.toDocumentList(
                documentList.stream()
                        .map(workspaceConverter::toDocument)
                        .toList()
        );
    }

    @Override
    public WorkspaceResponseDTO.DocumentSidebarList getDocumentWithoutLastOpenedList(User user, Workspace workspace) {

        // 유저가 가장 최근에 조회한 문서 5개 추출
        List<UserDocumentLastOpened> documentList = userDocumentLastOpenedJpaRepository.findRecentDocuments(user.getId(), workspace.getId(), PageRequest.of(0,8));

        return WorkspaceConverter.toDocumentSidebarList(
                documentList.stream()
                        .map(workspaceConverter::toDocumentSidebar)
                        .toList()
        );
    }

    @Override
    public WorkspaceResponseDTO.DocumentCalendarList getDocumentForCalendar(User user, Workspace workspace, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 워크스페이스에 속하면서 생성 날짜가 startDate, endDate 사이인 문서 리스트 검색
        List<Meeting> meetingList = meetingRepository.findAllDocumentForCalendars(workspace.getId(), startDateTime, endDateTime);
        List<SnsEvent> snsEventList = snsEventRepository.findAllDocumentForCalendars(workspace.getId(), startDateTime, endDateTime);
        List<MoodTracker> moodTrackerList = moodTrackerRepository.findAllDocumentForCalendars(workspace.getId(), startDateTime, endDateTime);

        // 모든 문서 합치기
        return workspaceConverter.toDocumentCalendarList(meetingList, snsEventList, moodTrackerList);
    }

    @Override
    public WorkspaceResponseDTO.WorkspaceEditPage getEditPage(User user, Workspace workspace) {

        List<UserResponseDTO.MemberInfo> memberInfoList = userWorkspaceJpaRepository.findUsersByWorkspaceId(workspace.getId()).stream()
                .map(UserConverter::toMemberInfo)
                .toList();

        String imageUrl = amazonS3Manager.generatePresignedUrl(workspace.getKeyName());

        return workspaceConverter.toWorkspaceEditPage(workspace, memberInfoList, imageUrl);
    }

    @Override
    public WorkspaceResponseDTO.RecentDocumentList getRecentDocuments(User user, Workspace workspace) {

        List<UserDocumentLastOpened> recentDocumentList = userDocumentLastOpenedJpaRepository.findRecentDocuments(user.getId(), workspace.getId(), PageRequest.of(0,8));

        return workspaceConverter.toRecentDocumentList(
                recentDocumentList.stream()
                        .map(workspaceConverter::toRecentDocument)
                        .toList()
        );

    }
}

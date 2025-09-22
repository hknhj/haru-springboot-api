package com.haru.api.meeting.application.service;

import com.haru.api.infra.websocket.WebSocketSessionRegistry;
import com.haru.api.meeting.application.port.in.EndMeetingUseCase;
import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndMeetingService implements EndMeetingUseCase {

    private final WebSocketSessionRegistry webSocketSessionRegistry;

    @Override
    @Transactional
    public void endMeeting(User user, Meeting meeting) {

        Long meetingId = meeting.getId();

        // 웹소켓 연결 종료 및 세션 삭제
        try {
            webSocketSessionRegistry.getSession(meetingId).close(CloseStatus.BAD_DATA.withReason("Invalid path"));
            webSocketSessionRegistry.removeSession(meetingId);
        } catch (Exception e) {
            log.error("meetingId: {} session 종료 오류", meetingId);
        }
    }
}

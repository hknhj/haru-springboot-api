package com.haru.api.meeting.application.port.in;

import com.haru.api.meeting.domain.Meeting;
import com.haru.api.user.domain.User;

public interface EndMeetingUseCase {

    /**
     * 회의가 종료되면 웹소켓 연결 종료 및 세션을 삭제하는 메서드
     *
     * @param user
     * @param meeting
     */
    void endMeeting(User user, Meeting meeting);

}

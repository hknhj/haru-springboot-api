package com.haru.api.meeting.application.event;

import com.haru.api.infra.websocket.AudioSessionBuffer;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MeetingEndedEvent extends ApplicationEvent {

    private final AudioSessionBuffer sessionBuffer;

    public MeetingEndedEvent(Object source, AudioSessionBuffer sessionBuffer) {
        super(source);
        this.sessionBuffer = sessionBuffer;
    }
}

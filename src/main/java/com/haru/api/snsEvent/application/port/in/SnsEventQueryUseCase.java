package com.haru.api.snsEvent.application.port.in;

import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

public interface SnsEventQueryUseCase {

    SnsEventResponseDTO.GetSnsEventListRequest getSnsEventList(User user, Workspace workspace);

    SnsEventResponseDTO.GetSnsEventRequest getSnsEvent(User user, SnsEvent snsEvent);

    SnsEventResponseDTO.getInstagramAccountName getInstagramAccountName(User user, Workspace workspace);
}

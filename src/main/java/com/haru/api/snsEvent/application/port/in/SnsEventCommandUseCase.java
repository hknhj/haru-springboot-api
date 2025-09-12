package com.haru.api.snsEvent.application.port.in;

import com.haru.api.snsEvent.presentation.dto.SnsEventRequestDTO;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.enums.Format;
import com.haru.api.snsEvent.domain.enums.ListType;
import com.haru.api.user.domain.User;
import com.haru.api.workspace.domain.Workspace;

public interface SnsEventCommandUseCase {

    SnsEventResponseDTO.CreateSnsEventResponse createSnsEvent(User user, Workspace workspace, SnsEventRequestDTO.CreateSnsRequest request);

    void updateSnsEvent(User user, SnsEvent snsEvent, SnsEventRequestDTO.UpdateSnsEventRequest request);

    void deleteSnsEvent(User user, SnsEvent snsEvent);

    SnsEventResponseDTO.ListDownLoadLinkResponse downloadList(User user, SnsEvent snsEvent, ListType listType, Format format);

}

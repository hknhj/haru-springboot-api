package com.haru.api.snsEvent.application.port.in;

import com.haru.api.snsEvent.domain.enums.InstagramRedirectType;
import com.haru.api.snsEvent.presentation.dto.SnsEventResponseDTO;
import com.haru.api.workspace.domain.Workspace;

public interface LinkInstagramAccountUseCase {

    SnsEventResponseDTO.LinkInstagramAccountResponse linkInstagramAccount(
            String code,
            Workspace workspace,
            InstagramRedirectType instagramRedirectType
    );

}

package com.haru.api.snsEvent.application.port.in;

import com.haru.api.snsEvent.domain.SnsEvent;

public interface UploadFileAndThumbnailUseCase {

    String createAndUploadListFileAndThumbnail(SnsEvent snsEvent);

}

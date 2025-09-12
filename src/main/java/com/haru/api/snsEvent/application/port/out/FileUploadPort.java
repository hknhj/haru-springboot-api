package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.SnsEvent;

public interface FileUploadPort {

    void uploadFile(String keyName, byte[] fileBytes, String contentType);

    void deleteSnsEventFileAndThumbnailImage(SnsEvent snsEvent);

}

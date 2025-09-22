package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.SnsEvent;

public interface FilePort {

    void uploadFile(String keyName, byte[] fileBytes, String contentType);

    void deleteSnsEventFileAndThumbnailImage(SnsEvent snsEvent);

    String getDownloadLink(String keyName, String fileName);

}

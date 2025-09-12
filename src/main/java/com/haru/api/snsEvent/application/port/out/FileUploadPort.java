package com.haru.api.snsEvent.application.port.out;

public interface FileUploadPort {

    void uploadFile(String keyName, byte[] fileBytes, String contentType);

}

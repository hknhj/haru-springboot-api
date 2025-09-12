package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.snsEvent.application.port.out.FileUploadPort;
import com.haru.api.snsEvent.domain.SnsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUploadImpl implements FileUploadPort {

    private final AmazonS3Manager amazonS3Manager;

    @Override
    public void uploadFile(String keyName, byte[] fileBytes, String contentType) {
        amazonS3Manager.uploadFile(keyName, fileBytes, contentType);
    }

    @Override
    public void deleteSnsEventFileAndThumbnailImage(SnsEvent snsEvent) {
        amazonS3Manager.deleteFile(snsEvent.getKeyNameParticipantPdf());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameParticipantWord());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameWinnerPdf());
        amazonS3Manager.deleteFile(snsEvent.getKeyNameWinnerWord());
        amazonS3Manager.deleteFile(snsEvent.getThumbnailKeyName());
    }
}

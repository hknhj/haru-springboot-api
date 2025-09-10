package com.haru.api.meeting.infrastructure.adapter;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MeetingHandler;
import com.haru.api.infra.mp3encoder.Mp3EncoderService;
import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.meeting.application.port.out.AudioUploadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class S3AudioUploadService implements AudioUploadPort {

    private final Mp3EncoderService encoderService;
    private final AmazonS3Manager amazonS3Manager;

    @Override
    public String uploadAudioFile(ByteArrayOutputStream audioBuffer) {
        try {
            byte[] rawAudioData = audioBuffer.toByteArray();

            int channels = 1;
            int samplingRate = 16000;
            int bitRate = 128000;
            byte[] mp3Data = encoderService.encodePcmToMp3(rawAudioData, channels, samplingRate, bitRate);

            String keyName = amazonS3Manager.generateKeyName("meeting/recording") + ".mp3";
            amazonS3Manager.uploadFile(keyName, mp3Data, "audio/mpeg");

            return keyName;

        } catch (Exception e) {
            throw new MeetingHandler(ErrorStatus.MEETING_AUDIO_FILE_UPLOAD_FAIL);
        }
    }
}

package com.haru.api.meeting.application.port.out;

import java.io.ByteArrayOutputStream;

public interface AudioUploadPort {

    /**
     * 전체 회의 음성 파일을 s3에 업로드하고, audio file key name을 저장하는 메서드
     *
     * @param audioBuffer : 현재 처리하는 세션의 전체 원본 음성 데이터
     *
     */
    String uploadAudioFile(ByteArrayOutputStream audioBuffer);

}

package com.haru.api.global.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface FileExtractorService {

    /**
     * MultipartFile을 받아 파일 형식에 따라 텍스트를 추출합니다.
     */
    String extractTextFromFile(MultipartFile file);

}

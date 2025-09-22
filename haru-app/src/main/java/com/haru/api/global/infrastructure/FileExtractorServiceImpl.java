package com.haru.api.global.infrastructure;

import com.haru.api.global.apiPayload.code.status.ErrorStatus;
import com.haru.api.global.apiPayload.exception.handler.MeetingHandler;
import com.haru.api.global.application.port.FileExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.StringWriter;

@Slf4j
@Component
public class FileExtractorServiceImpl implements FileExtractorService {

    @Override
    public String extractTextFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        String filename = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                // PDF에서 텍스트 추출
                try (PDDocument document = PDDocument.load(inputStream)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (filename != null && filename.toLowerCase().endsWith(".docx")) {
                // DOCX에서 텍스트 추출
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream);

                StringWriter stringWriter = new StringWriter();
                TextUtils.extractText(wordMLPackage.getMainDocumentPart(), stringWriter);
                return stringWriter.toString();
            } else {
                log.warn("지원하지 않는 파일 형식입니다: {}", filename);
                return "";
            }
        } catch (Exception e) {
            throw new MeetingHandler(ErrorStatus.MEETING_FILE_UPLOAD_FAIL);
        }
    }
}

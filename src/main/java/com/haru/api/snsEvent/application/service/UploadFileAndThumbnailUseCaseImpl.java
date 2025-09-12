package com.haru.api.snsEvent.application.service;

import com.haru.api.global.apiPayload.exception.handler.SnsEventHandler;
import com.haru.api.global.util.file.FileConvertHelper;
import com.haru.api.infra.s3.AmazonS3Manager;
import com.haru.api.infra.s3.MarkdownFileUploader;
import com.haru.api.snsEvent.application.port.in.UploadFileAndThumbnailUseCase;
import com.haru.api.snsEvent.application.port.out.*;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.domain.enums.ListType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static com.haru.api.global.apiPayload.code.status.ErrorStatus.SNS_EVENT_DOWNLOAD_LIST_ERROR;

@Component
@RequiredArgsConstructor
public class UploadFileAndThumbnailUseCaseImpl implements UploadFileAndThumbnailUseCase {

    private final ParticipantPort participantPort;
    private final WinnerPort winnerPort;
    private final HtmlGenerationPort htmlGenerationPort;
    private final FileConvertHelper fileConvertHelper;;
    private final AmazonS3Manager amazonS3Manager;
    private final FileConversionPort fileConversionPort;
    private final MarkdownFileUploader markdownFileUploader;

    @Override
    public String createAndUploadListFileAndThumbnail(SnsEvent snsEvent) {
        List<Participant> participantList = participantPort.findAllBySnsEventId(snsEvent.getId());
        List<Winner> winnerList = winnerPort.findAllBySnsEventId(snsEvent.getId());

        String listHtmlParticipant = htmlGenerationPort.generateHtmlFromList(participantList);
        String listHtmlWinner = htmlGenerationPort.generateHtmlFromList(winnerList);

        byte[] pdfBytesParticipant;
        byte[] pdfBytesWinner;
        byte[] docxBytesParticipant;
        byte[] docxBytesWinner;
        try {
            // 1) 폰트를 스트림/바이트로 읽기
            byte[] fontBytes;
            try (InputStream in = getClass().getClassLoader()
                    .getResourceAsStream("templates/NotoSansKR-Regular.ttf")) {
                if (in == null) throw new IllegalStateException("Font not found on classpath");
                fontBytes = in.readAllBytes();
            }
            // 폰트 경로
            listHtmlParticipant = fileConvertHelper.injectPageMarginStyle(listHtmlParticipant);
            listHtmlWinner = fileConvertHelper.injectPageMarginStyle(listHtmlWinner);
            byte[] shiftedPdfBytesParticipant = fileConvertHelper.convertHtmlToPdf(listHtmlParticipant, fontBytes);
            byte[] shiftedPdfBytesWinner = fileConvertHelper.convertHtmlToPdf(listHtmlWinner, fontBytes);
            pdfBytesParticipant =  fileConversionPort.addTitleToPdf(shiftedPdfBytesParticipant, snsEvent.getTitle() + " 참여자 리스트", fontBytes);
            pdfBytesWinner =  fileConversionPort.addTitleToPdf(shiftedPdfBytesWinner, snsEvent.getTitle() + " 당첨자 리스트", fontBytes);
            docxBytesParticipant =  fileConversionPort.createWord(ListType.PARTICIPANT, snsEvent.getTitle() + " 참여자 리스트", snsEvent);
            docxBytesWinner =  fileConversionPort.createWord(ListType.WINNER, snsEvent.getTitle() + " 당첨자 리스트", snsEvent );
        } catch (Exception e) {
            throw new SnsEventHandler(SNS_EVENT_DOWNLOAD_LIST_ERROR);
        }
        // PDF, DOCS파일, 썸네일 S3에 업로드 및 DB에 keyName저장
        String fullPath = "sns-event/" + snsEvent.getId();
        String keyNameParticipantPdf = amazonS3Manager.generateKeyName(fullPath) + "." + "pdf";
        String keyNameParticipantWord = amazonS3Manager.generateKeyName(fullPath) + "." + "docx";
        String keyNameWinnerPdf = amazonS3Manager.generateKeyName(fullPath) + "." + "pdf";
        String keyNameWinnerWord = amazonS3Manager.generateKeyName(fullPath) + "." + "docx";
        amazonS3Manager.uploadFile(keyNameParticipantPdf, pdfBytesParticipant, "application/pdf");
        amazonS3Manager.uploadFile(keyNameWinnerPdf, pdfBytesWinner, "application/pdf");
        amazonS3Manager.uploadFile(keyNameParticipantWord, docxBytesParticipant, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        amazonS3Manager.uploadFile(keyNameWinnerWord, docxBytesWinner, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        // SNS 이벤트에 keyName 저장
        snsEvent.updateKeyNameParticipantPdf(
                keyNameParticipantPdf,
                keyNameParticipantWord,
                keyNameWinnerPdf,
                keyNameWinnerWord
        );

        // SNS 이벤트 당첨자 PDF의 첫페이지 썸네일로 S3에 업로드
        return markdownFileUploader.createOrUpdateThumbnailWithPdfBytes(
                pdfBytesWinner,
                "sns-event",
                null
        );
    }
}

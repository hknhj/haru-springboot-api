package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.FileConversionPort;
import com.haru.api.snsEvent.application.port.out.ParticipantPort;
import com.haru.api.snsEvent.application.port.out.WinnerPort;
import com.haru.api.snsEvent.domain.Participant;
import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.Winner;
import com.haru.api.snsEvent.domain.enums.ListType;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileConversionAdapter implements FileConversionPort {

    private final ParticipantPort participantPort;
    private final WinnerPort winnerPort;

    private final int WORD_TABLE_SIZE = 40; // 페이지당 총 아이디 수
    private final int PER_COL = WORD_TABLE_SIZE/ 2; // 한쪽 컬럼에 들어갈 개수

    @Override
    public byte[] convertHtmlToPdf(String listHtml, byte[] fontBytes) {
        // Openhtmltopdf/Flying Saucer를 사용하여 PDF 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(listHtml, null); // ex) "file:/opt/app/static/" or "https://your.cdn/", // base url 설정, 직접css파일 가져오거나 프론트엔드 배포 후 적용
        builder.toStream(baos);
        // 한글 폰트 임베딩
        // byte[] → 임시 파일
        try{
            if (fontBytes != null && fontBytes.length > 0) {
                Path tmpFont = Files.createTempFile("NotoSansKR-", ".ttf");
                Files.write(tmpFont, fontBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                builder.useFont(tmpFont.toFile(), "NotoSansKR");
            }
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] addTitleToPdf(byte[] pdfBytes, String title, byte[] fontBytes) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        PdfStamper stamper = new PdfStamper(reader, out);
        // byte[]로 폰트 임베딩 (경로 X)
        // 첫 번째 인자 name은 식별용 문자열이라 임의명 가능, 실제 폰트는 byte[]에서 읽힙니다.
        BaseFont bf = BaseFont.createFont(
                "NotoSansKR-Regular.ttf",         // internal name (아무 문자열 OK)
                BaseFont.IDENTITY_H,          // 유니코드 CJK
                BaseFont.EMBEDDED,            // 폰트 임베드
                false,                        // cached (메모리 캐시 안 함)
                fontBytes,                    // TTF 바이트
                null                          // PFB (Type1용, TTF면 null)
        );
//        BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        int totalPages = reader.getNumberOfPages();
        for (int i = 1; i <= totalPages; i++) {
            PdfContentByte over = stamper.getOverContent(i);
            over.beginText();
            over.setFontAndSize(bf, 26f); // 글씨 크게 (28pt)
            // 페이지 폭 중앙 계산
            float x = reader.getPageSize(i).getWidth() / 2;
            // 페이지 상단에서 약간 내려오게 (70pt 여백)
            float y = reader.getPageSize(i).getTop() - 70f;
            over.showTextAligned(Element.ALIGN_CENTER, title, x, y, 0);
            over.endText();
        }
        stamper.close();
        reader.close();
        return out.toByteArray();
    }

    @Override
    public byte[] createWord(ListType listType, String listTitle, SnsEvent snsEvent) {
        List<String> list = new ArrayList<>();
        if (listType == ListType.PARTICIPANT) {
            List<Participant> participantList = participantPort.findAllBySnsEventId(snsEvent.getId());
            for (Participant participant : participantList) {
                list.add(participant.getNickname());
            }
            return createTable(list, listTitle);
        } else {
            List<Winner> winnerList = winnerPort.findAllBySnsEventId(snsEvent.getId());
            for (Winner winner : winnerList) {
                list.add(winner.getNickname());
            }
            return createTable(list, listTitle);
        }
    }

    private byte[] createTable(List<String> list, String listTitle)  {
        int page = list.size() / WORD_TABLE_SIZE + (list.size() % WORD_TABLE_SIZE == 0 ? 0 : 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XWPFDocument doc = new XWPFDocument();
        for (int p = 0; p < page; p++) {
            int pageStart = p * WORD_TABLE_SIZE;
            int pageEnd = Math.min(pageStart + WORD_TABLE_SIZE, list.size());
            // 첫 페이지에만 제목 추가
            if (p == 0) {
                addTitle(doc, listTitle, 22);
            }
            // ── 현재 페이지 테이블: (헤더 1행 + 데이터 18행) × 4열 [번호, ID, 번호, ID]
            // 열 너비를 twip 단위로 설정 (1cm ≈ 567 twip)
            // [번호, ID, 번호, ID] 순서
            int[] colWidths = {1000, 3000, 1000, 3000};
            XWPFTable table = doc.createTable(PER_COL + 1, 4); // +1은 헤더
            setColumnWidths(table, colWidths);
            // 스타일(테두리/정렬)
            table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
            table.setTableAlignment(TableRowAlign.CENTER);
            // 헤더
            setCellTextCentered(table.getRow(0).getCell(0), "번호", true);
            setCellTextCentered(table.getRow(0).getCell(1), "ID",   true);
            setCellTextCentered(table.getRow(0).getCell(2), "번호", true);
            setCellTextCentered(table.getRow(0).getCell(3), "ID",   true);
            // 데이터 채우기
            for (int i = 0; i < PER_COL; i++) {
                int rowIdx = i + 1; // 헤더 다음 줄부터
                int leftIdx  = pageStart + i;               // 왼쪽 컬럼 번호 시작
                int rightIdx = pageStart + PER_COL + i;     // 오른쪽 컬럼 번호 시작
                // 왼쪽
                if (leftIdx < pageEnd) {
                    setCellTextCentered(table.getRow(rowIdx).getCell(0), String.valueOf(leftIdx + 1), false);
                    setCellTextLeft    (table.getRow(rowIdx).getCell(1), list.get(leftIdx), false);
                } else {
                    clearCell(table.getRow(rowIdx).getCell(0));
                    clearCell(table.getRow(rowIdx).getCell(1));
                }
                // 오른쪽
                if (rightIdx < pageEnd) {
                    setCellTextCentered(table.getRow(rowIdx).getCell(2), String.valueOf(rightIdx + 1), false);
                    setCellTextLeft    (table.getRow(rowIdx).getCell(3), list.get(rightIdx), false);
                } else {
                    clearCell(table.getRow(rowIdx).getCell(2));
                    clearCell(table.getRow(rowIdx).getCell(3));
                }
                // 행 분할 금지(페이지 넘어가며 쪼개지지 않도록)
                try { table.getRow(rowIdx).setCantSplitRow(true); } catch (Throwable ignored) {}
            }
            // 마지막 페이지가 아니면 페이지 브레이크
            if (p < page - 1) {
                XWPFParagraph br = doc.createParagraph();
                XWPFRun r = br.createRun();
                r.addBreak(BreakType.PAGE);
            }
        }
        try {
            doc.write(baos);
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void clearCell(XWPFTableCell cell) {
        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) cell.removeParagraph(i);
        cell.addParagraph(); // 빈 문단 하나 유지
    }

    private void setCellTextCentered(XWPFTableCell cell, String text, boolean bold) {
        setCellText(cell, text, ParagraphAlignment.CENTER, bold);
    }

    private void setCellTextLeft(XWPFTableCell cell, String text, boolean bold) {
        setCellText(cell, text, ParagraphAlignment.LEFT, bold);
    }

    private void setCellText(XWPFTableCell cell, String text, ParagraphAlignment align, boolean bold) {
        if (!cell.getParagraphs().isEmpty()) cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(align);
        XWPFRun r = p.createRun();
        r.setFontSize(11);
        r.setBold(bold);
        r.setText(text);
    }

    private void setColumnWidths(XWPFTable table, int[] colWidths) {
        // 표 전체 너비 고정
        table.setWidthType(TableWidthType.DXA);
        int totalWidth = 0;
        for (int w : colWidths) totalWidth += w;
        table.setWidth(String.valueOf(totalWidth));
        for (int col = 0; col < colWidths.length; col++) {
            for (XWPFTableRow row : table.getRows()) {
                XWPFTableCell cell = row.getCell(col);
                cell.setWidthType(TableWidthType.DXA);
                cell.setWidth(String.valueOf(colWidths[col]));
            }
        }
    }

    // 제목 추가 메소드
    private void addTitle(XWPFDocument doc, String titleText, int fontSize) {
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER); // 가운데 정렬
        XWPFRun run = title.createRun();
        run.setText(titleText);
        run.setFontSize(fontSize);  // 전달받은 크기로 설정
        run.setBold(true);             // 굵게
        run.addBreak();                // 제목과 표 사이 한 줄 띄움
    }
}

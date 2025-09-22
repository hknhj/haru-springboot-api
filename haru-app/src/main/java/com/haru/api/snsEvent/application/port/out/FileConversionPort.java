package com.haru.api.snsEvent.application.port.out;

import com.haru.api.snsEvent.domain.SnsEvent;
import com.haru.api.snsEvent.domain.enums.ListType;

public interface FileConversionPort {

    byte[] convertHtmlToPdf(String listHtml, byte[] fontBytes);

    byte[] addTitleToPdf(byte[] pdfBytes, String title, byte[] fontBytes) throws Exception;

    byte[] createWord(ListType listType, String listTitle, SnsEvent snsEvent);

}

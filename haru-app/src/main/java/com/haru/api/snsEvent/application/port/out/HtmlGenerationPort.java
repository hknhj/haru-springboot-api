package com.haru.api.snsEvent.application.port.out;

import java.util.List;

public interface HtmlGenerationPort {

    String generateHtmlFromList(List<?> list);

}

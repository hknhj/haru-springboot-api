package com.haru.api.snsEvent.infrastructure.adapter;

import com.haru.api.snsEvent.application.port.out.HtmlGenerationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ThymeleafHtmlGenerator implements HtmlGenerationPort {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String generateHtmlFromList(List<?> list) {

        int total = list.size();
        int mid = (total + 1) / 2;
        List<?> leftList = list.subList(0, mid);
        List<?> rightList = list.subList(mid, total);

        // Thymeleaf context에 데이터 세팅
        Context context = new Context();
        context.setVariable("leftList", leftList);
        context.setVariable("rightList", rightList);

        return templateEngine.process("sns-event-list-pdf-template", context);
    }
}

package com.charlyghislain.dispatcher.management.converter;

import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.management.api.domain.WsMailHeaders;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MailHeadersTemplateConverter {

    public MailHeadersTemplate toMailHeadersTemplate(WsMailHeaders wsMailHeaders) {
        String from = wsMailHeaders.getFrom();
        String to = wsMailHeaders.getTo();
        String cc = wsMailHeaders.getCc();
        String bcc = wsMailHeaders.getBcc();
        String subject = wsMailHeaders.getSubject();

        MailHeadersTemplate mailHeaders = new MailHeadersTemplate();
        mailHeaders.setFrom(from);
        mailHeaders.setTo(to);
        mailHeaders.setCc(cc);
        mailHeaders.setBcc(bcc);
        mailHeaders.setSubject(subject);
        return mailHeaders;
    }
}

package com.charlyghislain.dispatcher.management.converter;


import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.management.api.domain.WsMailHeaders;

import javax.enterprise.context.ApplicationScoped;
import javax.mail.Address;
import java.util.Set;

@ApplicationScoped
public class WsMailHeadersConverter {

    public WsMailHeaders toWsMailHeaders(MailHeadersTemplate headerTemplate) {
        String from = headerTemplate.getFrom();
        String to = headerTemplate.getTo();
        String cc = headerTemplate.getCc();
        String bcc = headerTemplate.getBcc();
        String subject = headerTemplate.getSubject();

        WsMailHeaders mailHeaders = new WsMailHeaders();
        mailHeaders.setFrom(from);
        mailHeaders.setTo(to);
        mailHeaders.setCc(cc);
        mailHeaders.setBcc(bcc);
        mailHeaders.setSubject(subject);
        return mailHeaders;
    }

    public WsMailHeaders toWsMailHeaders(RenderedMailHeaders renderedMailHeaders) {
        Address from = renderedMailHeaders.getFrom();
        Set<Address> to = renderedMailHeaders.getTo();
        Set<Address> cc = renderedMailHeaders.getCc();
        Set<Address> bcc = renderedMailHeaders.getBcc();
        String subject = renderedMailHeaders.getSubject();

        String toString = to.stream()
                .map(Address::toString)
                .map(a -> a + ";")
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();

        String ccString = cc.stream()
                .map(Address::toString)
                .map(a -> a + ";")
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();

        String bccString = bcc.stream()
                .map(Address::toString)
                .map(a -> a + ";")
                .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                .toString();

        WsMailHeaders mailHeaders = new WsMailHeaders();
        mailHeaders.setFrom(from.toString());
        mailHeaders.setTo(toString);
        mailHeaders.setCc(ccString);
        mailHeaders.setBcc(bccString);
        mailHeaders.setSubject(subject);
        return mailHeaders;
    }
}

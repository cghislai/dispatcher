package com.charlyghislain.dispatcher.api.dispatching;


import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.header.MessageHeaders;
import com.charlyghislain.dispatcher.api.header.SmsHeadersTemplate;

public enum DispatchingOption {

    MAIL_HTML("mail-html", MailHeadersTemplate.class),
    MAIL_TEXT("mail-text", MailHeadersTemplate.class),
    SMS("sms", SmsHeadersTemplate.class);

    String templateFileName;
    Class<? extends MessageHeaders> headersType;

    DispatchingOption(String templateFileName, Class<? extends MessageHeaders> headersType) {
        this.templateFileName = templateFileName;
        this.headersType = headersType;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public Class<? extends MessageHeaders> getHeadersType() {
        return headersType;
    }
}

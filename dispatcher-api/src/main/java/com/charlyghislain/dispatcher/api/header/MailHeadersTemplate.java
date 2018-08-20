package com.charlyghislain.dispatcher.api.header;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MailHeadersTemplate implements MessageHeaders {

    @NotNull
    private String from;
    @NotNull
    @Size(min = 1)
    private String to;
    @Nullable
    private String cc;
    @Nullable
    private String bcc;
    @NotNull
    private String subject;

    public boolean isEmpty() {
        return from == null && to == null;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

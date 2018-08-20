package com.charlyghislain.dispatcher.api.header;

public class SmsHeadersTemplate implements MessageHeaders {

    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public SmsHeadersTemplate setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public SmsHeadersTemplate setTo(String to) {
        this.to = to;
        return this;
    }
}

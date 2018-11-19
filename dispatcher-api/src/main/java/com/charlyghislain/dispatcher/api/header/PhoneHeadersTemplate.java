package com.charlyghislain.dispatcher.api.header;

public class PhoneHeadersTemplate implements MessageHeaders {

    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public PhoneHeadersTemplate setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public PhoneHeadersTemplate setTo(String to) {
        this.to = to;
        return this;
    }
}

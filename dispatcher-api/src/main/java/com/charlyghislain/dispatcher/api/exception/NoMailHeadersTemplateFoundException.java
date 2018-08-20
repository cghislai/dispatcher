package com.charlyghislain.dispatcher.api.exception;

import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.Locale;

public class NoMailHeadersTemplateFoundException extends DispatcherException {

    private DispatcherMessage message;
    private Locale locale;

    public NoMailHeadersTemplateFoundException(DispatcherMessage message, Locale locale) {
        super("Could not find required mail headers for message " + message.getName() + " and locale " + locale);
        this.message = message;
        this.locale = locale;
    }
}

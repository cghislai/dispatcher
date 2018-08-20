package com.charlyghislain.dispatcher.api.exception;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.Locale;

public class NoTemplateFoundException extends DispatcherException {

    private DispatcherMessage message;
    private DispatchingOption option;
    private Locale locale;

    public NoTemplateFoundException(DispatcherMessage message, DispatchingOption option, Locale locale) {
        super("Could not find any template for message " + message.getName() + ", dispatching option " + option.name() + " and locale " + locale);
        this.message = message;
        this.option = option;
        this.locale = locale;
    }
}

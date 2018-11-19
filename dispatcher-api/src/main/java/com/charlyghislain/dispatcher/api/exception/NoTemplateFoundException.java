package com.charlyghislain.dispatcher.api.exception;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.Locale;

public class NoTemplateFoundException extends MessageRenderingException {

    private DispatcherMessage message;
    private RenderingOption option;
    private Locale locale;

    public NoTemplateFoundException(DispatcherMessage message, RenderingOption option, Locale locale) {
        super("Could not find any template for message " + message.getName() + ", dispatching option " + option.name() + " and locale " + locale);
        this.message = message;
        this.option = option;
        this.locale = locale;
    }
}

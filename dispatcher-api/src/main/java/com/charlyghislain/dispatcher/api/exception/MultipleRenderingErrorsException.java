package com.charlyghislain.dispatcher.api.exception;

import java.util.Arrays;
import java.util.List;

public class MultipleRenderingErrorsException extends MessageRenderingException {


    public MultipleRenderingErrorsException(String message) {
        super(message);
    }

    public MultipleRenderingErrorsException(String message, MessageRenderingException... causes) {
        super(message);
        Arrays.stream(causes).forEach(this::addSuppressed);
    }

    public MultipleRenderingErrorsException(String message, List<MessageRenderingException> renderingExceptions) {
        super(message);
        renderingExceptions.forEach(this::addSuppressed);
    }

}

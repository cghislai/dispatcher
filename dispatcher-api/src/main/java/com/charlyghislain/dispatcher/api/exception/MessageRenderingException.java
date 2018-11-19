package com.charlyghislain.dispatcher.api.exception;

public class MessageRenderingException extends DispatcherException {

    public MessageRenderingException() {
    }

    public MessageRenderingException(String message) {
        super(message);
    }

    public MessageRenderingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageRenderingException(Throwable cause) {
        super(cause);
    }

    public MessageRenderingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package com.charlyghislain.dispatcher.api.exception;

public class DispatcherRuntimeException extends RuntimeException {
    public DispatcherRuntimeException() {
    }

    public DispatcherRuntimeException(String message) {
        super(message);
    }

    public DispatcherRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatcherRuntimeException(Throwable cause) {
        super(cause);
    }

    public DispatcherRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

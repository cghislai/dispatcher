package com.charlyghislain.dispatcher.api.exception;

import java.util.Arrays;
import java.util.List;

public class MultipleDispatchingErrorsException extends DispatcherException {


    public MultipleDispatchingErrorsException(String message) {
        super(message);
    }

    public MultipleDispatchingErrorsException(String message, Throwable... causes) {
        super(message);
        Arrays.stream(causes).forEach(this::addSuppressed);
    }

    public MultipleDispatchingErrorsException(String message, List<? extends Throwable> errors) {
        super(message);
        errors.forEach(this::addSuppressed);
    }


}

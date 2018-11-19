package com.charlyghislain.dispatcher.api.dispatching;

import com.charlyghislain.dispatcher.api.exception.MultipleDispatchingErrorsException;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DispatchedMessage {

    private final Locale locale;
    private final DispatcherMessage message;
    private final List<DispatchingResult> dispatchingResultList = new ArrayList<>();
    private boolean allSucceeded;
    private boolean anySucceeded;

    public DispatchedMessage(DispatcherMessage message, Locale locale) {
        this.message = message;
        this.locale = locale;
    }

    public MultipleDispatchingErrorsException getMultiErrorsException() {
        List<Exception> errors = dispatchingResultList.stream()
                .filter(r -> !r.isSuccess())
                .map(DispatchingResult::getError)
                .collect(Collectors.toList());
        return new MultipleDispatchingErrorsException("Failed to dospatch some messages", errors);
    }

    public List<DispatchingResult> getDispatchingResultList() {
        return dispatchingResultList;
    }

    public Locale getLocale() {
        return locale;
    }

    public DispatcherMessage getMessage() {
        return message;
    }

    public boolean isAllSucceeded() {
        return allSucceeded;
    }

    public void setAllSucceeded(boolean allSucceeded) {
        this.allSucceeded = allSucceeded;
    }

    public boolean isAnySucceeded() {
        return anySucceeded;
    }

    public void setAnySucceeded(boolean anySucceeded) {
        this.anySucceeded = anySucceeded;
    }
}

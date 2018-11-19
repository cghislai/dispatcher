package com.charlyghislain.dispatcher.api.dispatching;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DispatchedMessage {

    private final Locale locale;
    private final List<DispatchingResult> dispatchingResultList = new ArrayList<>();

    public DispatchedMessage(Locale locale) {
        this.locale = locale;
    }

    public List<DispatchingResult> getDispatchingResultList() {
        return dispatchingResultList;
    }

    public Locale getLocale() {
        return locale;
    }
}

package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RenderedMessage {

    @NotNull
    private final Locale renderedLocale;
    @NotNull
    private final DispatcherMessage message;
    @NotNull
    private final List<RenderedMessageDispatchingOption> renderedMessageDispatchingOptions = new ArrayList<>();

    public RenderedMessage(@NotNull Locale renderedLocale, @NotNull DispatcherMessage message) {
        this.renderedLocale = renderedLocale;
        this.message = message;
    }

    public Locale getRenderedLocale() {
        return renderedLocale;
    }

    public List<RenderedMessageDispatchingOption> getRenderedMessageDispatchingOptions() {
        return renderedMessageDispatchingOptions;
    }

    public DispatcherMessage getMessage() {
        return message;
    }
}

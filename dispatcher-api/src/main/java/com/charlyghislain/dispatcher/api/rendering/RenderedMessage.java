package com.charlyghislain.dispatcher.api.rendering;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RenderedMessage {

    @NotNull
    private final Locale renderedLocale;
    @NotNull
    private final List<RenderedMessageDispatchingOption> renderedMessageDispatchingOptions = new ArrayList<>();

    public RenderedMessage(@NotNull Locale renderedLocale) {
        this.renderedLocale = renderedLocale;
    }

    public Locale getRenderedLocale() {
        return renderedLocale;
    }

    public List<RenderedMessageDispatchingOption> getRenderedMessageDispatchingOptions() {
        return renderedMessageDispatchingOptions;
    }
}

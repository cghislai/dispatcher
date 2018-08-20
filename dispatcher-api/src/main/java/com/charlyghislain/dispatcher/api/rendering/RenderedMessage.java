package com.charlyghislain.dispatcher.api.rendering;

import org.checkerframework.checker.nullness.qual.Nullable;

public class RenderedMessage {

    @Nullable
    private RenderedMailMessage renderedMailMessage;

    public RenderedMailMessage getRenderedMailMessage() {
        return renderedMailMessage;
    }

    public RenderedMessage setRenderedMailMessage(RenderedMailMessage renderedMailMessage) {
        this.renderedMailMessage = renderedMailMessage;
        return this;
    }
}

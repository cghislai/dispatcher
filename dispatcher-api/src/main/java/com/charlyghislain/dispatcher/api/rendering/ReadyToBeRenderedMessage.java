package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReadyToBeRenderedMessage {

    @NotNull
    private DispatcherMessage message;
    @NotNull
    private List<Locale> acceptedLocales;
    @NotNull
    private List<TemplateContextObject> contextObjects;
    @NotNull
    private Set<DispatchingOption> dispatchingOptions;
    @NotNull
    private Set<String> referencedResources;
    @NotNull
    private RenderingType renderingType;

    @Nullable
    private MailHeadersTemplate mailHeadersTemplate;


    public MailHeadersTemplate getMailHeadersTemplate() {
        return mailHeadersTemplate;
    }

    public ReadyToBeRenderedMessage setMailHeadersTemplate(MailHeadersTemplate mailHeadersTemplate) {
        this.mailHeadersTemplate = mailHeadersTemplate;
        return this;
    }

    public List<Locale> getAcceptedLocales() {
        return acceptedLocales;
    }

    public ReadyToBeRenderedMessage setAcceptedLocales(List<Locale> acceptedLocales) {
        this.acceptedLocales = acceptedLocales;
        return this;
    }

    public List<TemplateContextObject> getContextObjects() {
        return contextObjects;
    }

    public ReadyToBeRenderedMessage setContextObjects(List<TemplateContextObject> contextObjects) {
        this.contextObjects = contextObjects;
        return this;
    }

    public Set<DispatchingOption> getDispatchingOptions() {
        return dispatchingOptions;
    }

    public ReadyToBeRenderedMessage setDispatchingOptions(Set<DispatchingOption> dispatchingOptions) {
        this.dispatchingOptions = dispatchingOptions;
        return this;
    }

    public DispatcherMessage getMessage() {
        return message;
    }

    public ReadyToBeRenderedMessage setMessage(DispatcherMessage message) {
        this.message = message;
        return this;
    }

    public Set<String> getReferencedResources() {
        return referencedResources;
    }

    public ReadyToBeRenderedMessage setReferencedResources(Set<String> referencedResources) {
        this.referencedResources = referencedResources;
        return this;
    }

    public RenderingType getRenderingType() {
        return renderingType;
    }

    public ReadyToBeRenderedMessage setRenderingType(RenderingType renderingType) {
        this.renderingType = renderingType;
        return this;
    }
}

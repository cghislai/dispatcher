package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * A message ready to be rendered.
 *
 * @see ReadyToBeRenderedMessageBuilder
 * @see com.charlyghislain.dispatcher.service.MessageRendererService#renderMessage(com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage)
 */
public class ReadyToBeRenderedMessage {

    @NotNull
    private final DispatcherMessage message;
    @NotNull
    private final List<Locale> acceptedLocales = new ArrayList<>();
    @NotNull
    private final List<TemplateContextObject> contextObjects = new ArrayList<>();
    @NotNull
    private final List<DispatchingRenderingOption> dispatchingRenderingOptionsByOrderOfPreference = new ArrayList<>();
    @NotNull
    private final Set<String> referencedResources = new HashSet<>();
    @NotNull
    private RenderingMedia renderingMedia = RenderingMedia.NORMAL;
    /**
     * If true, rendering will fail if any of the dispatching option preferences fail.
     */
    private boolean requireAllDispatchingOptionToRender;
//
//    @Nullable
//    private MailHeadersTemplate mailHeadersTemplate;

    /**
     * @see {@link ReadyToBeRenderedMessageBuilder}
     */
    ReadyToBeRenderedMessage(DispatcherMessage message) {
        this.message = message;
    }

    public DispatcherMessage getMessage() {
        return message;
    }


    public List<Locale> getAcceptedLocales() {
        return acceptedLocales;
    }


    public List<TemplateContextObject> getContextObjects() {
        return contextObjects;
    }


    public Set<String> getReferencedResources() {
        return referencedResources;
    }


    public RenderingMedia getRenderingMedia() {
        return renderingMedia;
    }

    public void setRenderingMedia(RenderingMedia renderingMedia) {
        this.renderingMedia = renderingMedia;
    }
//
//    public MailHeadersTemplate getMailHeadersTemplate() {
//        return mailHeadersTemplate;
//    }
//
//    public void setMailHeadersTemplate(MailHeadersTemplate mailHeadersTemplate) {
//        this.mailHeadersTemplate = mailHeadersTemplate;
//    }

    public List<DispatchingRenderingOption> getDispatchingRenderingOptionsByOrderOfPreference() {
        return dispatchingRenderingOptionsByOrderOfPreference;
    }

    public boolean isRequireAllDispatchingOptionToRender() {
        return requireAllDispatchingOptionToRender;
    }

    public void setRequireAllDispatchingOptionToRender(boolean requireAllDispatchingOptionToRender) {
        this.requireAllDispatchingOptionToRender = requireAllDispatchingOptionToRender;
    }
}

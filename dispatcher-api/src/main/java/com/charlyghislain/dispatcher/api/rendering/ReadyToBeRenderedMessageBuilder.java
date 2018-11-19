package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class ReadyToBeRenderedMessageBuilder {

    private final ReadyToBeRenderedMessage readyToBeRenderedMessage;

    public static ReadyToBeRenderedMessageBuilder newBuider(DispatcherMessage message) {
        return new ReadyToBeRenderedMessageBuilder(message);
    }

    private ReadyToBeRenderedMessageBuilder(DispatcherMessage message, TemplateContextObject... contextObjects) {
        readyToBeRenderedMessage = new ReadyToBeRenderedMessage(message);
        withContext(contextObjects);
    }

    public ReadyToBeRenderedMessage build() {
        return readyToBeRenderedMessage;
    }


    /**
     * Append mail dispatching to the dispatching options preference list, accepting any rendered
     * LONG_HTML and LONG_TEXT templates.
     */
    public ReadyToBeRenderedMessageBuilder acceptMailDispatching() {
        DispatchingRenderingOption mailRenderingOptions = DispatchingOption.MAIL.anyRenderingOption(RenderingOption.LONG_HTML, RenderingOption.LONG_TEXT);
        readyToBeRenderedMessage.getDispatchingRenderingOptionsByOrderOfPreference().add(mailRenderingOptions);
        return this;
    }

    /**
     * Append a dispatching option to the preference list.
     *
     * @param dispatchingRenderingOption
     * @return
     */
    public ReadyToBeRenderedMessageBuilder acceptDispatchingOption(DispatchingRenderingOption dispatchingRenderingOption) {
        readyToBeRenderedMessage.getDispatchingRenderingOptionsByOrderOfPreference().add(dispatchingRenderingOption);
        return this;
    }


    public ReadyToBeRenderedMessageBuilder acceptLocale(Locale locale) {
        readyToBeRenderedMessage.getAcceptedLocales().add(locale);
        return this;
    }

    public ReadyToBeRenderedMessageBuilder acceptLocales(Collection<Locale> locales) {
        readyToBeRenderedMessage.getAcceptedLocales().addAll(locales);
        return this;
    }

    public ReadyToBeRenderedMessageBuilder withContext(TemplateContextObject... contextObjects) {
        Arrays.stream(contextObjects).forEach(readyToBeRenderedMessage.getContextObjects()::add);
        return this;
    }

    public ReadyToBeRenderedMessageBuilder withContext(Collection<TemplateContextObject> contextObjects) {
        readyToBeRenderedMessage.getContextObjects().addAll(contextObjects);
        return this;
    }

    public ReadyToBeRenderedMessageBuilder forWebRendering() {
        readyToBeRenderedMessage.setRenderingMedia(RenderingMedia.WEB_PAGE);
        return this;
    }

    public ReadyToBeRenderedMessageBuilder forMailRendering() {
        readyToBeRenderedMessage.setRenderingMedia(RenderingMedia.NORMAL);
        return this;
    }

    /**
     * By default, errors are silenced as long as a message could be rendered for any of the dispatching option in the
     * preference list. Calling this method requires all dispatching options added via the accept* methods to render
     * successfully. Note that a single DispatchingOption may render successfully even if only a subset of its
     * RenderingOption rendered successfully, see {@link DispatchingRenderingOption}.
     */
    public ReadyToBeRenderedMessageBuilder requireAllDispatchingOptionsToRender() {
        readyToBeRenderedMessage.setRequireAllDispatchingOptionToRender(true);
        return this;
    }

}

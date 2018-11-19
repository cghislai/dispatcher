package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.MessageRenderingException;
import com.charlyghislain.dispatcher.api.rendering.*;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.NoTemplateFoundException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public interface MessageRenderer {

    /**
     * Render a message using the following logic:
     * <ul>
     * <li>
     * Accepted locales are iterated over until a message could be rendered. For each of them:
     * </li>
     * <li>
     * Dispatching options are iterated over. Depending on {@link ReadyToBeRenderedMessage#isRequireAllDispatchingOptionToRender},
     * rendering will fail if a message could not be rendered for none or not all of them. For each of them, rendering
     * is attempted using {@link MessageRenderer#renderForLocaleAndOption(com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage, java.util.Locale, com.charlyghislain.dispatcher.api.rendering.DispatchingRenderingOption)}
     * </li>
     * </ul>
     *
     * @param readyToBeRenderedMessage
     * @return
     * @throws MessageRenderingException
     */
    RenderedMessage renderMessage(ReadyToBeRenderedMessage readyToBeRenderedMessage) throws MessageRenderingException;

    /**
     * Render the message for the given locale, ignoring {@link ReadyToBeRenderedMessage#getAcceptedLocales()}.
     * See {@link MessageRenderer#renderMessage(com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage)}
     * for rendering logic.
     *
     * @param readyToBeRenderedMessage
     * @param locale
     * @return
     * @throws MessageRenderingException
     */
    RenderedMessage renderForLocale(ReadyToBeRenderedMessage readyToBeRenderedMessage, Locale locale) throws MessageRenderingException;

    /**
     * Render a message for a given dispatching option, ignoring those values from the ReadyToBeRenderedMessage parameter,
     * using the following logic:
     * <ul>
     * <li>
     * Message header rendering is attempted.
     * </li>
     * <li>
     * Rendering options are iterated over. Depending on {@link DispatchingRenderingOption#isAcceptAny()},
     * rendering will fail if a message could not be rendered for none or not all of them. For each of them,
     * message rendering is attempted.
     * </li>
     * </ul>
     *
     * @param locale            The locale
     * @param dispatchingOption The dispatching option with rendering preference.
     * @return
     * @throws MessageRenderingException
     */
    <T extends RenderedMessageDispatchingOption> T renderForLocaleAndOption(ReadyToBeRenderedMessage readyToBeRenderedMessage, Locale locale, DispatchingRenderingOption dispatchingOption)
            throws MessageRenderingException;

    RenderedTemplate renderTemplate(DispatcherMessage message, RenderingOption renderingOption, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingMedia renderingMedia) throws MessageRenderingException;

    RenderedMailHeaders renderMailHeaders(MailHeadersTemplate mailHeadersTemplate, Locale locale, List<TemplateContextObject> templateContexts) throws MessageRenderingException;

    InputStream streamNonRenderedTemplate(DispatcherMessage dispatcherMessage, RenderingOption renderingOption, Locale locale) throws MessageRenderingException;
}

package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.NoTemplateFoundException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderingType;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public interface MessageRenderer {

    RenderedMessage renderMessage(ReadyToBeRenderedMessage readyToBeRenderedMessage) throws DispatcherException;

    InputStream streamNonRenderedTemplate(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption, Locale locale) throws NoTemplateFoundException;

    RenderedTemplate renderTemplate(DispatcherMessage message, DispatchingOption dispatchingOption, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingType renderingType) throws NoTemplateFoundException, DispatcherException;

    RenderedMailHeaders renderMailHeaders(MailHeadersTemplate mailHeadersTemplate, Locale locale, List<TemplateContextObject> templateContexts);

}

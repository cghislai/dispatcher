package com.charlyghislain.dispatcher.management.service;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.MessageRenderingException;
import com.charlyghislain.dispatcher.api.exception.NoMailHeadersTemplateFoundException;
import com.charlyghislain.dispatcher.api.exception.NoTemplateFoundException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderingMedia;
import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.service.MessageRenderer;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class MultiLocaleMessageService {

    @Inject
    private StreamingService streamingService;
    @Inject
    private MessageRenderer messageRenderer;
    @Inject
    private MessageResourcesService messageResourcesService;

    public StreamingOutput streamMessageTemplateForFirstMatchingLanguage(List<Locale> acceptableLanguages, DispatcherMessage dispatcherMessage, RenderingOption renderingOption) {
        for (Locale locale : acceptableLanguages) {
            try {
                InputStream templateStream = messageRenderer.streamNonRenderedTemplate(dispatcherMessage, renderingOption, locale);
                return this.streamingService.streamOutput(templateStream);
            } catch (MessageRenderingException e) {
                // try next language
            }
        }
        throw new DispatcherWebException(DispatcherWebError.TEMPLATE_NOT_FOUND, "No template found for the provided languages");
    }

    public StreamingOutput streamRenderedExampleTemplateForFirstMatchingLanguage(List<Locale> acceptableLanguages, DispatcherMessage dispatcherMessage, RenderingOption renderingOption, List<TemplateContextObject> templateContextObjects) throws DispatcherException {
        RenderedTemplate renderedTemplate = this.getRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, dispatcherMessage, renderingOption, templateContextObjects, RenderingMedia.WEB_PAGE);
        return this.streamingService.streamOutput(renderedTemplate.getContentStream());
    }

    public RenderedTemplate getRenderedExampleTemplateForFirstMatchingLanguage(List<Locale> acceptableLanguages, DispatcherMessage dispatcherMessage, RenderingOption renderingOption, List<TemplateContextObject> templateContextObjects, RenderingMedia renderingMedia) throws DispatcherException {
        for (Locale locale : acceptableLanguages) {
            try {
                return messageRenderer.renderTemplate(dispatcherMessage, renderingOption, locale, templateContextObjects, renderingMedia);
            } catch (NoTemplateFoundException e) {
                // try next language
            }
        }
        throw new DispatcherWebException(DispatcherWebError.TEMPLATE_NOT_FOUND, "No template found for the provided languages");
    }

    public MailHeadersTemplate findFirstMailHeadersTemplateMatchingLanguages(List<Locale> acceptableLanguages, DispatcherMessage dispatcherMessage) {
        for (Locale locale : acceptableLanguages) {
            try {
                return messageResourcesService.findMailMessageHeaders(dispatcherMessage, locale);
            } catch (NoMailHeadersTemplateFoundException e) {
                // try next language
            }
        }
        throw new DispatcherWebException(DispatcherWebError.TEMPLATE_NOT_FOUND, "No required mail headers found for the provided languages");
    }

    public RenderedMailHeaders renderFirstMailHeadersTemplateMatchingLanguages(List<Locale> acceptableLanguages, DispatcherMessage dispatcherMessage, List<TemplateContextObject> templateContextObjects) {
        for (Locale locale : acceptableLanguages) {
            try {
                MailHeadersTemplate mailMessageHeaders = messageResourcesService.findMailMessageHeaders(dispatcherMessage, locale);
                return messageRenderer.renderMailHeaders(mailMessageHeaders, locale, templateContextObjects);
            } catch (NoMailHeadersTemplateFoundException | MessageRenderingException e) {
                // try next language
            }
        }
        throw new DispatcherWebException(DispatcherWebError.TEMPLATE_NOT_FOUND, "No required mail headers found for the provided languages");
    }

}
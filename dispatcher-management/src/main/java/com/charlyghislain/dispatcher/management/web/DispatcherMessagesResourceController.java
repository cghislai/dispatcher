package com.charlyghislain.dispatcher.management.web;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.filter.DispatcherMessageFilter;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.api.service.MessageResourcesUpdateService;
import com.charlyghislain.dispatcher.api.service.TemplateContextsService;
import com.charlyghislain.dispatcher.management.api.DispatcherMessagesResource;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessage;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessageFilter;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatchingOption;
import com.charlyghislain.dispatcher.management.api.domain.WsMailHeaders;
import com.charlyghislain.dispatcher.management.api.domain.WsMessageTemplateVariable;
import com.charlyghislain.dispatcher.management.api.domain.WsResultList;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.converter.DispatcherMessageFilterConverter;
import com.charlyghislain.dispatcher.management.converter.DispatchingOptionConverter;
import com.charlyghislain.dispatcher.management.converter.MailHeadersTemplateConverter;
import com.charlyghislain.dispatcher.management.converter.WsDispatcherMessageConverter;
import com.charlyghislain.dispatcher.management.converter.WsMailHeadersConverter;
import com.charlyghislain.dispatcher.management.converter.WsMessageTemplateVariableConverter;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;
import com.charlyghislain.dispatcher.management.service.LocalesService;
import com.charlyghislain.dispatcher.management.service.MultiLocaleMessageService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.charlyghislain.dispatcher.management.api.security.DispatcherManagementRoles.ROLE_MANAGER;

@RolesAllowed(ROLE_MANAGER)
public class DispatcherMessagesResourceController implements DispatcherMessagesResource {

    @Inject
    private MailHeadersTemplateConverter mailHeadersTemplateConverter;
    @Inject
    private DispatcherMessageFilterConverter dispatcherMessageFilterConverter;
    @Inject
    private DispatchingOptionConverter dispatchingOptionConverter;
    @Inject
    private WsDispatcherMessageConverter wsDispatcherMessageConverter;
    @Inject
    private WsMailHeadersConverter wsMailHeadersConverter;
    @Inject
    private WsMessageTemplateVariableConverter wsMessageTemplateVariableConverter;

    @Inject
    private MessageResourcesService messageResourcesService;
    @Inject
    private MessageResourcesUpdateService messageResourcesUpdateService;
    @Inject
    private TemplateContextsService templateContextsService;

    @Inject
    private LocalesService localesService;
    @Inject
    private MultiLocaleMessageService multiLocaleMessageService;

    @Override
    public WsResultList<WsDispatcherMessage> listAllMessages(WsDispatcherMessageFilter messageFilter) {
        DispatcherMessageFilter dispatcherMessageFilter = dispatcherMessageFilterConverter.toDispatcherMessageFilter(messageFilter);
        List<WsDispatcherMessage> wsDispatcherMessages = messageResourcesService.findMessages(dispatcherMessageFilter)
                .stream()
                .map(wsDispatcherMessageConverter::toWsDispatcherMessage)
                .collect(Collectors.toList());
        return new WsResultList<>(wsDispatcherMessages, wsDispatcherMessages.size());
    }

    @Override
    public WsDispatcherMessage getMessage(String name) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        return wsDispatcherMessageConverter.toWsDispatcherMessage(dispatcherMessage);
    }

    @Override
    public StreamingOutput streamMessageTemplate(String name, WsDispatchingOption wsDispatchingOption, HttpHeaders httpHeaders) {
        DispatchingOption dispatchingOption = dispatchingOptionConverter.toDispatchingOption(wsDispatchingOption);
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));

        return multiLocaleMessageService.streamMessageTemplateForFirstMatchingLanguage(acceptableLanguages, dispatcherMessage, dispatchingOption);
    }


    @Override
    public void updateMessageTemplate(String name, WsDispatchingOption wsDispatchingOption, String languageTag, InputStream body) {
        DispatchingOption dispatchingOption = dispatchingOptionConverter.toDispatchingOption(wsDispatchingOption);
        Locale locale = localesService.getLocale(languageTag);
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));

        messageResourcesUpdateService.setMessageTemplateContent(dispatcherMessage, dispatchingOption, locale, body);
    }

    @Override
    public void updateRootLocaleMessageTemplate(String name, WsDispatchingOption wsDispatchingOption, InputStream body) {
        DispatchingOption dispatchingOption = dispatchingOptionConverter.toDispatchingOption(wsDispatchingOption);
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));

        messageResourcesUpdateService.setMessageTemplateContent(dispatcherMessage, dispatchingOption, Locale.ROOT, body);
    }

    @Override
    public StreamingOutput streamRenderedMessageTemplateExampleHtml(String name, WsDispatchingOption wsDispatchingOption, HttpHeaders httpHeaders) {
        return streamRenderedExampleTemplate(name, wsDispatchingOption, httpHeaders);
    }

    @Override
    public StreamingOutput steamRenderedMessageTemplateExamplePlainText(String name, WsDispatchingOption wsDispatchingOption, HttpHeaders httpHeaders) {
        return streamRenderedExampleTemplate(name, wsDispatchingOption, httpHeaders);
    }

    private StreamingOutput streamRenderedExampleTemplate(String name, WsDispatchingOption wsDispatchingOption, HttpHeaders httpHeaders) {
        DispatchingOption dispatchingOption = dispatchingOptionConverter.toDispatchingOption(wsDispatchingOption);
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createExampleTemplateContexts(dispatcherMessage);

        try {
            return multiLocaleMessageService.streamRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, dispatcherMessage, dispatchingOption, exampleTemplateContexts);
        } catch (DispatcherException e) {
            throw new DispatcherWebException(DispatcherWebError.TEMPLATE_RENDERING_ERROR, e.getMessage());
        }
    }

    @Override
    public List<String> getAvailableLocalesWithContent(String name, WsDispatchingOption wsDispatchingOption) {
        DispatchingOption dispatchingOption = dispatchingOptionConverter.toDispatchingOption(wsDispatchingOption);
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));

        return messageResourcesService.streamLocalesWithExistingVelocityTemplateOrResourceBundle(dispatcherMessage, dispatchingOption)
                .map(Locale::toLanguageTag)
                .collect(Collectors.toList());
    }

    @Override
    public WsMailHeaders getMailHeadersTemplate(String name, HttpHeaders httpHeaders) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);

        MailHeadersTemplate mailHeadersTemplate = multiLocaleMessageService.findFirstMailHeadersTemplateMatchingLanguages(acceptableLanguages, dispatcherMessage);
        return wsMailHeadersConverter.toWsMailHeaders(mailHeadersTemplate);
    }

    @Override
    public void updateMailHeadersTemplate(String name, String languageTag, WsMailHeaders templateMailHeaders) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        MailHeadersTemplate headersTemplate = mailHeadersTemplateConverter.toMailHeadersTemplate(templateMailHeaders);
        Locale locale = localesService.getLocale(languageTag);

        messageResourcesUpdateService.setMailHeadersTemplate(dispatcherMessage, locale, headersTemplate);
    }

    @Override
    public void updateRootLocaleMailHeadersTemplate(String name, WsMailHeaders templateMailHeaders) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        MailHeadersTemplate headersTemplate = mailHeadersTemplateConverter.toMailHeadersTemplate(templateMailHeaders);

        messageResourcesUpdateService.setMailHeadersTemplate(dispatcherMessage, Locale.ROOT, headersTemplate);
    }

    @Override
    public WsMailHeaders getRenderedMailHeadersExample(String name, HttpHeaders httpHeaders) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createExampleTemplateContexts(dispatcherMessage);

        RenderedMailHeaders renderedMailHeaders = multiLocaleMessageService.renderFirstMailHeadersTemplateMatchingLanguages(acceptableLanguages, dispatcherMessage, exampleTemplateContexts);
        return wsMailHeadersConverter.toWsMailHeaders(renderedMailHeaders);
    }

    @Override
    public List<WsMessageTemplateVariable> getMessageTemplateVariableDescriptions(String name) {
        DispatcherMessage dispatcherMessage = messageResourcesService.findMessageByName(name)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.MESSAGE_NOT_FOUND, "No message named " + name));
        return templateContextsService.listTemplateVariableDescriptions(dispatcherMessage)
                .stream()
                .map(wsMessageTemplateVariableConverter::toWsMessageTemplateVariable)
                .collect(Collectors.toList());
    }
}

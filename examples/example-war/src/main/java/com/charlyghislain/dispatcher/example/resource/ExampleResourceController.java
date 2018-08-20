package com.charlyghislain.dispatcher.example.resource;

import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingResult;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.SharedResourceNotFoundException;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.MailAttachment;
import com.charlyghislain.dispatcher.api.message.Message;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderingType;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.api.service.TemplateContextsService;
import com.charlyghislain.dispatcher.example.message.ExampleMessageA;
import com.charlyghislain.dispatcher.example.message.ExampleMessageB;
import com.charlyghislain.dispatcher.management.api.domain.WsMailHeaders;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.converter.WsMailHeadersConverter;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;
import com.charlyghislain.dispatcher.management.service.LocalesService;
import com.charlyghislain.dispatcher.management.service.MultiLocaleMessageService;
import com.charlyghislain.dispatcher.service.MailDispatcherService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Path("/example/{message}")
@RolesAllowed("user")
public class ExampleResourceController {

    @Inject
    private TemplateContextsService templateContextsService;
    @Inject
    private MailDispatcherService mailDispatcherService;
    @Inject
    private MessageResourcesService messageResourcesService;

    @Inject
    private LocalesService localesService;
    @Inject
    private MultiLocaleMessageService multiLocaleMessageService;
    @Inject
    private WsMailHeadersConverter wsMailHeadersConverter;

    @Inject
    @Message(ExampleMessageA.class)
    private DispatcherMessage exampleMessageA;

    @Inject
    @Message(ExampleMessageB.class)
    private DispatcherMessage exampleMessageB;

    @PathParam("message")
    private String messageName;

    @GET
    @Path("/mail/html")
    @Produces(MediaType.TEXT_HTML + ";charset=UTF-8")
    public StreamingOutput getRenderedHtmlMessage(@Context HttpHeaders httpHeaders) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());

        try {
            return multiLocaleMessageService.streamRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, getMessage(), DispatchingOption.MAIL_HTML, exampleTemplateContexts);
        } catch (DispatcherException e) {
            throw new DispatcherWebException(DispatcherWebError.TEMPLATE_RENDERING_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("/mail/text")
    @Produces(MediaType.TEXT_PLAIN)
    public StreamingOutput getRenderedTextMessage(@Context HttpHeaders httpHeaders) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());

        try {
            return multiLocaleMessageService.streamRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, getMessage(), DispatchingOption.MAIL_TEXT, exampleTemplateContexts);
        } catch (DispatcherException e) {
            throw new DispatcherWebException(DispatcherWebError.TEMPLATE_RENDERING_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("/sms")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public StreamingOutput getRenderedSmsMessage(@Context HttpHeaders httpHeaders) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());

        try {
            return multiLocaleMessageService.streamRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, getMessage(), DispatchingOption.SMS, exampleTemplateContexts);
        } catch (DispatcherException e) {
            throw new DispatcherWebException(DispatcherWebError.TEMPLATE_RENDERING_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("/mail/headers")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public WsMailHeaders getRenderedMailHeaders(@Context HttpHeaders httpHeaders) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());

        RenderedMailHeaders renderedMailHeaders = multiLocaleMessageService.renderFirstMailHeadersTemplateMatchingLanguages(acceptableLanguages, exampleMessageA, exampleTemplateContexts);
        return wsMailHeadersConverter.toWsMailHeaders(renderedMailHeaders);
    }


    @GET
    @Path("/mail/mime")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public StreamingOutput renderMimeMessage(@Context HttpHeaders httpHeaders) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());

        try {
            RenderedMailMessage mailMessage = renderMailMessage(acceptableLanguages, exampleTemplateContexts);
            MimeMessage mimeMessage = mailDispatcherService.createMimeMessage(mailMessage);
            return output -> {
                try {
                    mimeMessage.writeTo(output);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (DispatcherException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/mail/mime/send")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Object renderMimeMessage(@Context HttpHeaders httpHeaders, @QueryParam("to") String to) {
        List<Locale> acceptableLanguages = localesService.getAcceptedLanguages(httpHeaders);
        List<TemplateContextObject> exampleTemplateContexts = templateContextsService.createTemplateContexts(getMessage());


        try {
            Address address = new InternetAddress(to);

            RenderedMailMessage renderedMailMessage = renderMailMessage(acceptableLanguages, exampleTemplateContexts);
            renderedMailMessage.getMailMessageHeaders().getTo().add(address);

            Set<DispatchingResult> dispatchingResults = mailDispatcherService.dispatchMessages(renderedMailMessage);
            return dispatchingResults;

        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }


    private RenderedMailMessage renderMailMessage(List<Locale> acceptableLanguages, List<TemplateContextObject> exampleTemplateContexts) {
        RenderedMailHeaders renderedMailHeaders = multiLocaleMessageService.renderFirstMailHeadersTemplateMatchingLanguages(acceptableLanguages, getMessage(), exampleTemplateContexts);
        Map<DispatchingOption, RenderedTemplate> templates = new HashMap<>();

        try {
            // Render the available options for this message
            DispatcherMessage message = getMessage();
            message.getDispatchingOptions().stream()
                    .filter(option -> option == DispatchingOption.MAIL_HTML || option == DispatchingOption.MAIL_TEXT)
                    .forEach(option -> {
                        try {
                            RenderedTemplate renderedTemplate = multiLocaleMessageService.getRenderedExampleTemplateForFirstMatchingLanguage(acceptableLanguages, getMessage(), option, exampleTemplateContexts, RenderingType.MAIL);
                            templates.put(option, renderedTemplate);
                        } catch (DispatcherException e) {
                            throw new RuntimeException(e);
                        }
                    });

            RenderedMailMessage mailMessage = new RenderedMailMessage();
            mailMessage.setRenderedTemplates(templates);
            mailMessage.setMailMessageHeaders(renderedMailHeaders);


            // Add an attachment
            MailAttachment mailAttachment = new MailAttachment();
            InputStream attachmentContent = messageResourcesService.streamSharedResource(Paths.get("shared_resources/image1.jpeg"));
            mailAttachment.setContentStream(attachmentContent);
            mailAttachment.setFileName("image-test.jpg");
            mailAttachment.setMimetype("image/jpeg");
            List<MailAttachment> mailAttachments = Collections.singletonList(mailAttachment);


            mailMessage.setMailAttachments(mailAttachments);
            return mailMessage;
        } catch (SharedResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private DispatcherMessage getMessage() {
        switch (messageName) {
            case "a":
                return exampleMessageA;
            case "b":
                return exampleMessageB;
            default:
                throw new NotFoundException("No message named " + messageName);
        }
    }
}

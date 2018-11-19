package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingResult;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.message.MailAttachment;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.mail.MailSession;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Stateless
public class MailDispatcherService {

    private static final Logger LOG = LoggerFactory.getLogger(MailDispatcherService.class);

    @Inject
    @MailSession
    private Session mailSession;

    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_TRANSPORT_ENABLED,
            defaultValue = ConfigConstants.MAIL_TRANSPORT_ENABLED_DEFAULT_VALUE)
    private Boolean mailTransportEnabled;

    @Inject
    @ConfigProperty(name = ConfigConstants.MAIL_TRANSPORT_WHITELISTED_ADDRESSES_REGEXP,
            defaultValue = ConfigConstants.MAIL_TRANSPORT_WHITELISTED_ADDRESSES_REGEXP_DEFAULT_VALUE)
    private String whiteListedAddressRegexp;

    @Inject
    private MessageResourcesService messageResourcesService;


    public Set<DispatchingResult> dispatchMessages(RenderedMailMessage renderedMailMessage) {
        Map<RenderingOption, RenderedTemplate> renderedTemplates = renderedMailMessage.getRenderedTemplates();
        boolean hasTextTemplate = renderedTemplates.containsKey(RenderingOption.LONG_TEXT);
        boolean hasHtmlTemplate = renderedTemplates.containsKey(RenderingOption.LONG_HTML);

        if (!hasTextTemplate && !hasHtmlTemplate) {
            throw new DispatcherRuntimeException("No rendered template to dispatch by mail");
        }

        Set<DispatchingResult> dispatchingResults = new HashSet<>();

        try {
            MimeMessage mimeMessage = createMimeMessage(renderedMailMessage);
            String messageId = this.sendMail(mimeMessage);

            if (hasHtmlTemplate) {
                DispatchingResult successResult = createSuccessResult(messageId, RenderingOption.LONG_HTML);
                dispatchingResults.add(successResult);
            }
            if (hasTextTemplate) {
                DispatchingResult successResult = createSuccessResult(messageId, RenderingOption.LONG_TEXT);
                dispatchingResults.add(successResult);
            }

        } catch (DispatcherException e) {
            if (hasHtmlTemplate) {
                DispatchingResult errorResult = createErrorResult(RenderingOption.LONG_HTML, e);
                dispatchingResults.add(errorResult);
            }
            if (hasTextTemplate) {
                DispatchingResult errorResult = createErrorResult(RenderingOption.LONG_TEXT, e);
                dispatchingResults.add(errorResult);
            }
        }
        return dispatchingResults;
    }

    public MimeMessage createMimeMessage(RenderedMailMessage renderedMailMessage) throws DispatcherException {
        Map<RenderingOption, RenderedTemplate> renderedTemplates = renderedMailMessage.getRenderedTemplates();
        List<MailAttachment> mailAttachments = renderedMailMessage.getMailAttachments();
        RenderedMailHeaders renderedHeaders = renderedMailMessage.getRenderedHeaders();
        boolean hasTextTemplate = renderedTemplates.containsKey(RenderingOption.LONG_TEXT);
        boolean hasHtmlTemplate = renderedTemplates.containsKey(RenderingOption.LONG_HTML);


        MimeMessage mimeMessage = createMimeMessageHeaders(renderedHeaders);
        MimeMultipart multipart = new MimeMultipart();

        try {
            multipart.setSubType("alternative");
            if (hasTextTemplate) {
                RenderedTemplate renderedTemplate = renderedTemplates.get(RenderingOption.LONG_TEXT);
                InputStream contentStream = renderedTemplate.getContentStream();
                BodyPart textBodyPart = this.createTextBodyPart(contentStream);
                multipart.addBodyPart(textBodyPart);
            }

            if (hasHtmlTemplate) {
                RenderedTemplate renderedTemplate = renderedTemplates.get(RenderingOption.LONG_HTML);
                InputStream contentStream = renderedTemplate.getContentStream();
                BodyPart htmlBodyPart = this.createHtmlBodyPart(contentStream);
                multipart.addBodyPart(htmlBodyPart);

                Set<ReferencedResource> referencedResources = renderedTemplate.getReferencedResources();
                for (ReferencedResource referencedResource : referencedResources) {
                    BodyPart embeddedResourcePart = this.createEmbeddedResourcePart(referencedResource);
                    multipart.addBodyPart(embeddedResourcePart);
                }
            }

            for (MailAttachment mailAttachment : mailAttachments) {
                BodyPart bodyPart = createAttachmentResourcePart(mailAttachment);
                multipart.addBodyPart(bodyPart);
            }


            mimeMessage.setContent(multipart);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new DispatcherException("Failed to construct mime message", e);
        }
    }


    private String sendMail(MimeMessage mimeMessage) throws DispatcherException {
        if (!mailTransportEnabled) {
            throw new DispatcherException("Mail transport is disabled by configuration");
        }
        try {
            // Fix javax.activation stuff?
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Transport.send(mimeMessage);

            String messageId = UUID.randomUUID().toString();
            LOG.info("Sent message {}", messageId);
            return messageId;
        } catch (MessagingException e) {
            throw new DispatcherException("Error while sending mail over transport", e);
        }
    }

    private BodyPart createTextBodyPart(InputStream inputStream) throws DispatcherException {
        MimeBodyPart textBodyPart = new MimeBodyPart();

        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String textContent = bufferedReader.lines()
                    .map(line -> line + "\n")
                    .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                    .toString();
            textBodyPart.setText(textContent, "UTF-8", "plain");
            return textBodyPart;

        } catch (MessagingException | IOException e) {
            throw new DispatcherException("Error while creating mail text body part", e);
        }
    }


    private BodyPart createHtmlBodyPart(InputStream inputStream) throws DispatcherException {
        MimeBodyPart htmlBodyPart = new MimeBodyPart();

        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String textContent = bufferedReader.lines()
                    .map(line -> line + "\n")
                    .reduce(new StringBuilder(), StringBuilder::append, StringBuilder::append)
                    .toString();
            htmlBodyPart.setText(textContent, "UTF-8", "html");
            return htmlBodyPart;

        } catch (MessagingException | IOException e) {
            throw new DispatcherException("Error while creating mail html body part", e);
        }
    }

    private BodyPart createEmbeddedResourcePart(ReferencedResource referencedResource) throws DispatcherException {
        String resourceId = referencedResource.getId();
        String mimeType = referencedResource.getMimeType();
        InputStream resourceStream = messageResourcesService.streamReferencedResource(referencedResource);


        try {
            DataSource dataSource = new ByteArrayDataSource(resourceStream, mimeType);
            DataHandler dataHandler = new DataHandler(dataSource);

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setDataHandler(dataHandler);
            bodyPart.setContentID("<" + resourceId + ">");
            bodyPart.setDisposition(Part.INLINE);
            return bodyPart;
        } catch (MessagingException | IOException e) {
            throw new DispatcherException("Error while creating embedded resource body part for resource id " + resourceId, e);
        }

    }


    private BodyPart createAttachmentResourcePart(MailAttachment mailAttachment) throws DispatcherException {
        String mimetype = mailAttachment.getMimetype();
        InputStream contentStream = mailAttachment.getContentStream();
        String fileName = mailAttachment.getFileName();

        try {
            DataSource dataSource = new ByteArrayDataSource(contentStream, mimetype);
            DataHandler dataHandler = new DataHandler(dataSource);

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setDataHandler(dataHandler);
            bodyPart.setFileName(fileName);
            bodyPart.setDisposition(Part.ATTACHMENT);
            return bodyPart;
        } catch (MessagingException | IOException e) {
            throw new DispatcherException("Error while creating mail attachment part for attachment " + fileName, e);
        }

    }


    private MimeMessage createMimeMessageHeaders(RenderedMailHeaders messageHeaders) throws DispatcherException {
        Address from = messageHeaders.getFrom();
        Set<Address> to = messageHeaders.getTo();
        Set<Address> cc = messageHeaders.getCc();
        Set<Address> bcc = messageHeaders.getBcc();
        String subject = messageHeaders.getSubject();

        try {
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            mimeMessage.setFrom(from);
            for (Address address : to) {
                this.addRecipient(mimeMessage, address, MimeMessage.RecipientType.TO);
            }
            for (Address address : cc) {
                this.addRecipient(mimeMessage, address, MimeMessage.RecipientType.CC);
            }
            for (Address address : bcc) {
                this.addRecipient(mimeMessage, address, MimeMessage.RecipientType.BCC);
            }
            mimeMessage.setSubject(subject);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new DispatcherException("Error while creating mail message header", e);
        }
    }

    private void addRecipient(MimeMessage mimeMessage, Address address, Message.RecipientType recipientType) throws DispatcherException {
        String addressString = address.toString();
        Pattern pattern = Pattern.compile(whiteListedAddressRegexp);
        Matcher matcher = pattern.matcher(addressString);
        if (!matcher.matches()) {
            LOG.info("Ignoring recipient address {} since it is not whitelisted in configuration", addressString);
            return;
        }
        try {
            mimeMessage.addRecipient(recipientType, address);
        } catch (MessagingException e) {
            throw new DispatcherException("Failed to add recipient " + recipientType, e);
        }
    }


    private DispatchingResult createSuccessResult(String messageId, RenderingOption renderingOption) {
        DispatchingResult dispatchingResult = new DispatchingResult(renderingOption, DispatchingOption.MAIL, true);
        dispatchingResult.setDispatchedTime(LocalDateTime.now());
        dispatchingResult.setMessageId(messageId);
        return dispatchingResult;
    }


    private DispatchingResult createErrorResult(RenderingOption renderingOption, Exception error) {
        String message = error.getMessage();
        DispatchingResult dispatchingResult = new DispatchingResult(renderingOption, DispatchingOption.MAIL, false);
        dispatchingResult.setError(error);
        dispatchingResult.setErrorMessage(message);
        return dispatchingResult;
    }
}

package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.exception.MessageRenderingException;
import com.charlyghislain.dispatcher.api.exception.MultipleRenderingErrorsException;
import com.charlyghislain.dispatcher.api.exception.NoMailHeadersTemplateFoundException;
import com.charlyghislain.dispatcher.api.exception.NoTemplateFoundException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.rendering.DispatchingRenderingOption;
import com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessageDispatchingOption;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderingMedia;
import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.service.MessageRenderer;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.mail.ReferencedResourceProvider;
import com.charlyghislain.dispatcher.util.TemplateResourcesTool;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.implement.IncludeRelativePath;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class MessageRendererService implements MessageRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageRendererService.class);
    public static final String TEMPLATE_FILE_HEADER_TAG = "##MSGRENDERER: ";
    public static final String RESOURCE_TOOL_CONTEXT_KEY = "res";

    @Inject
    @ConfigProperty(name = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH,
            defaultValue = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH_DEFAULT_VALUE)
    private Optional<String> filesystemResourcePath;
    @Inject
    @ConfigProperty(name = ConfigConstants.RESOURCES_BASE_DIR,
            defaultValue = ConfigConstants.RESOURCES_BASE_DIR_DEFAULT_VALUE)
    private Optional<String> resourceBaseDir;

    @Inject
    private MessageResourcesService messageResourcesService;
    @Inject
    private ReferencedResourceProvider referencedResourceProvider;

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void init() {
        Properties properties = new Properties();
        properties.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, IncludeRelativePath.class.getName());
        Optional<String> nonEmptyFilesystemResourcesPath = filesystemResourcePath
                .filter(val -> !val.trim().isEmpty());

        if (nonEmptyFilesystemResourcesPath.isPresent()) {
            String path = nonEmptyFilesystemResourcesPath.get();
            properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,class");
            properties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, path);
        } else {
            properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
        }
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty("runtime.log.name", "com.charlyghislain.dispatcher.velocity");

        velocityEngine = new VelocityEngine();
        velocityEngine.init(properties);
    }


    @Override
    public RenderedMessage renderMessage(ReadyToBeRenderedMessage readyToBeRenderedMessage) throws MessageRenderingException {
        // Accepted locales are iterated over until a message could be rendered.
        List<Locale> acceptedLocales = readyToBeRenderedMessage.getAcceptedLocales();
        List<MessageRenderingException> localesRenderingExceptions = new ArrayList<>();
        for (Locale locale : acceptedLocales) {
            try {
                RenderedMessage renderedMessage = renderForLocale(readyToBeRenderedMessage, locale);
                return renderedMessage;
            } catch (MessageRenderingException e) {
                String message = MessageFormat.format("Rendering for locale {0} failed with {1}", locale.toString(), e.getMessage());
                MessageRenderingException renderingException = new MessageRenderingException(message, e);
                localesRenderingExceptions.add(renderingException);
            }
        }

        String messageName = readyToBeRenderedMessage.getMessage().getName();
        String errorMessage = MessageFormat.format("Could not find any locale for which to render message {0}", messageName);
        MultipleRenderingErrorsException globalError = new MultipleRenderingErrorsException(errorMessage, localesRenderingExceptions);
        throw globalError;

    }

    @Override
    public RenderedMessage renderForLocale(ReadyToBeRenderedMessage readyToBeRenderedMessage, Locale locale) throws MessageRenderingException {
        // Dispatching options are iterated over. Depending on {@link ReadyToBeRenderedMessage#isRequireAllDispatchingOptionToRender},
        // rendering will fail if a message could not be rendered for none or not all of them

        List<DispatchingRenderingOption> dispatchingOptions = readyToBeRenderedMessage.getDispatchingRenderingOptionsByOrderOfPreference();
        boolean requireAllToRender = readyToBeRenderedMessage.isRequireAllDispatchingOptionToRender();
        DispatcherMessage message = readyToBeRenderedMessage.getMessage();
        List<MessageRenderingException> optionsRenderingExceptions = new ArrayList<>();

        RenderedMessage renderedMessage = new RenderedMessage(locale, message);
        List<RenderedMessageDispatchingOption> renderedMessageDispatchingOptions = renderedMessage.getRenderedMessageDispatchingOptions();
        for (DispatchingRenderingOption dispatchingOption : dispatchingOptions) {
            try {
                RenderedMessageDispatchingOption renderedMessageDispatchingOption = renderForLocaleAndOption(readyToBeRenderedMessage, locale, dispatchingOption);
                renderedMessageDispatchingOptions.add(renderedMessageDispatchingOption);
            } catch (MessageRenderingException e) {
                optionsRenderingExceptions.add(e);
            }
        }

        String messageName = message.getName();
        String optionsNames = dispatchingOptions.stream().map(DispatchingRenderingOption::getDispatchingOption)
                .map(Enum::name)
                .collect(Collectors.joining(","));
        if (requireAllToRender && !optionsRenderingExceptions.isEmpty()) {
            String errorMessage = MessageFormat.format("Could not render all dispatching option of message {0} for locale {1} and options {2}",
                    messageName, locale.toString(), optionsNames);
            throw new MultipleRenderingErrorsException(errorMessage, optionsRenderingExceptions);
        } else if (renderedMessageDispatchingOptions.isEmpty()) {
            String errorMessage = MessageFormat.format("Could not render any of the dispatching opttion for message {0}, locale {1} and options {2}",
                    messageName, locale.toString(), optionsNames);
            throw new MultipleRenderingErrorsException(errorMessage, optionsRenderingExceptions);
        }

        return renderedMessage;
    }

    @Override
    public RenderedMessageDispatchingOption renderForLocaleAndOption(ReadyToBeRenderedMessage readyToBeRenderedMessage, Locale locale, DispatchingRenderingOption dispatchingOption) throws MessageRenderingException {
        DispatchingOption option = dispatchingOption.getDispatchingOption();
        switch (option) {
            case MAIL:
                return renderMail(readyToBeRenderedMessage, locale, dispatchingOption);
            default:
                throw new MessageRenderingException("Not implemented");
        }
    }


    @Override
    public RenderedTemplate renderTemplate(DispatcherMessage message, RenderingOption renderingOption, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingMedia renderingMedia) throws MessageRenderingException {
        Optional<DispatcherMessage> messageHeader = message.getHeader();
        Optional<DispatcherMessage> messageFooter = message.getFooter();

        List<RenderedTemplate> templateList = new ArrayList<>();

        if (messageHeader.isPresent()) {
            RenderedTemplate renderedTemplate = this.renderTemplate(messageHeader.get(), renderingOption, locale, templateContextObjects, renderingMedia);
            templateList.add(renderedTemplate);
        }

        RenderedTemplate renderedContent = streamRenderedTemplatesForLocale(message, locale, renderingOption, templateContextObjects, renderingMedia)
                .findFirst()
                .orElseThrow(() -> new MessageRenderingException("Could not find any template for message " + message.getName() + ", option " + renderingOption + " and locale " + locale));
        templateList.add(renderedContent);

        if (messageFooter.isPresent()) {
            RenderedTemplate renderedTemplate = this.renderTemplate(messageFooter.get(), renderingOption, locale, templateContextObjects, renderingMedia);
            templateList.add(renderedTemplate);
        }

        return RenderedTemplate.compose(templateList.toArray(new RenderedTemplate[0]));
    }

    @Override
    public RenderedMailHeaders renderMailHeaders(MailHeadersTemplate mailHeadersTemplate, Locale locale, List<TemplateContextObject> templateContexts) throws MessageRenderingException {
        VelocityContext velocityContext = this.createVelocityContext(templateContexts, locale, RenderingMedia.NORMAL);

        RenderedMailHeaders renderedMailHeaders = new RenderedMailHeaders();
        this.renderSingleAddressHeaderField(mailHeadersTemplate::getFrom, renderedMailHeaders::setFrom, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getTo, renderedMailHeaders::setTo, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getCc, renderedMailHeaders::setCc, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getBcc, renderedMailHeaders::setBcc, velocityContext);
        this.renderTextHeaderField(mailHeadersTemplate::getSubject, renderedMailHeaders::setSubject, velocityContext);
        return renderedMailHeaders;
    }

    @Override
    public InputStream streamNonRenderedTemplate(DispatcherMessage dispatcherMessage, RenderingOption renderingOption, Locale locale) throws NoTemplateFoundException {
        return messageResourcesService.streamVelocityTemplatePaths(dispatcherMessage, renderingOption, locale)
                .map(this::streamRawTemplateContent)
                .findFirst()
                .orElseThrow(() -> new NoTemplateFoundException(dispatcherMessage, renderingOption, locale));
    }

    private RenderedMailMessage renderMail(ReadyToBeRenderedMessage readyToBeRenderedMessage, Locale locale, DispatchingRenderingOption dispatchingOption) throws MessageRenderingException {
        List<TemplateContextObject> contextObjects = readyToBeRenderedMessage.getContextObjects();
        DispatcherMessage message = readyToBeRenderedMessage.getMessage();
        String messageName = readyToBeRenderedMessage.getMessage().getName();
        List<RenderingOption> renderingOptions = dispatchingOption.getRenderingOptionsByOrderOfPreference();
        boolean acceptAnyRenderedTemplate = dispatchingOption.isAcceptAny();
        RenderingMedia renderingMedia = readyToBeRenderedMessage.getRenderingMedia();
        String optionsNames = renderingOptions.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));


        RenderedMailMessage mailMessage = new RenderedMailMessage();

        try {
            MailHeadersTemplate headersTemplate = messageResourcesService.findMailMessageHeaders(message, locale);
            RenderedMailHeaders renderedMailHeaders = renderMailHeaders(headersTemplate, locale, contextObjects);
            mailMessage.setRenderedHeaders(renderedMailHeaders);
        } catch (MessageRenderingException e) {
            String errorMessage = MessageFormat.format("Could not render mail headers of message {0} for locale {1}",
                    messageName, locale.toString());
            throw new MessageRenderingException(errorMessage, e);
        } catch (NoMailHeadersTemplateFoundException e) {
            String errorMessage = MessageFormat.format("Could not find mail headers template of message {0} for locale {1}",
                    messageName, locale.toString());
            throw new MessageRenderingException(errorMessage, e);
        }


        List<MessageRenderingException> templateRenderingExceptions = new ArrayList<>();
        for (RenderingOption renderingOption : renderingOptions) {
            try {
                RenderedTemplate renderedTemplate = renderTemplate(message, renderingOption, locale, contextObjects, renderingMedia);
                mailMessage.getRenderedTemplates().put(renderingOption, renderedTemplate);
            } catch (MessageRenderingException e) {
                templateRenderingExceptions.add(e);
            }
        }

        if (!acceptAnyRenderedTemplate && !templateRenderingExceptions.isEmpty()) {
            String errorMessage = MessageFormat.format("Could not render all templates of message {0} for locale {1} and options {2}",
                    messageName, locale.toString(), optionsNames);
            throw new MultipleRenderingErrorsException(errorMessage, templateRenderingExceptions);
        } else if (mailMessage.getRenderedTemplates().isEmpty()) {
            String errorMessage = MessageFormat.format("Could not render any templates of message {0} for locale {1} and options {2}",
                    messageName, locale.toString(), optionsNames);
            throw new MultipleRenderingErrorsException(errorMessage, templateRenderingExceptions);
        }

        return mailMessage;
    }


    private Stream<RenderedTemplate> streamRenderedTemplatesForLocale(DispatcherMessage dispatcherMessage, Locale locale, RenderingOption renderingOption, List<TemplateContextObject> templateContextObjects, RenderingMedia renderingMedia) {
        return messageResourcesService.streamVelocityTemplatePaths(dispatcherMessage, renderingOption, locale)
                .map(path -> this.tryStreamRenderedTemplateContent(path, locale, templateContextObjects, renderingMedia))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


    private void renderTextHeaderField(Supplier<String> templateSupplier, Consumer<String> consumer, VelocityContext velocityContext) {
        String template = templateSupplier.get();
        String renderedValue = this.renderVelocityString("mail-subject", template, velocityContext);
        String encodedValue = encodeMailHeader(renderedValue);
        consumer.accept(encodedValue);
    }

    private String encodeMailHeader(String renderedValue) {
        try {
            return MimeUtility.encodeText(renderedValue, "UTF-8", null);
        } catch (UnsupportedEncodingException e) {
            throw new DispatcherRuntimeException(e);
        }
    }

    private void renderMultiAddressesHeaderField(Supplier<String> templateSupplier, Consumer<Set<Address>> addressesConsumer, VelocityContext velocityContext) {
        String template = templateSupplier.get();
        if (template == null || template.trim().isEmpty()) {
            addressesConsumer.accept(new HashSet<>());
            return;
        }
        String[] addresses = template.split(";");
        Set<Address> renderedAddresses = Arrays.stream(addresses)
                .map(t -> this.tryRenderAddress(t, velocityContext))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        addressesConsumer.accept(renderedAddresses);
    }

    private void renderSingleAddressHeaderField(Supplier<String> templateSupplier, Consumer<Address> addressConsumer, VelocityContext velocityContext) {
        String template = templateSupplier.get();
        tryRenderAddress(template, velocityContext)
                .ifPresent(addressConsumer::accept);
    }

    private Optional<Address> tryRenderAddress(String template, VelocityContext velocityContext) {
        String renderedValue = this.renderVelocityString("mail-header", template, velocityContext);
        try {
//            String encodedValue = encodeMailHeader(renderedValue);
            Address address = new InternetAddress(renderedValue, true);
            return Optional.of(address);
        } catch (AddressException e) {
            // TODO: collect parsing errors
            LOG.warn("Failed to parse render mail address template {}: rendered value {} could not be parsed", template, renderedValue);
            return Optional.empty();
        }
    }

    private InputStream streamRawTemplateContent(Path relativePath) {
        String header = getTemplateFileHeader(relativePath);
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream headerStream = new ByteArrayInputStream(headerBytes);

        InputStream contentInputStream = messageResourcesService.streamVelocityTemplate(relativePath);
        SequenceInputStream sequenceInputStream = new SequenceInputStream(headerStream, contentInputStream);
        return sequenceInputStream;
    }

    @NotNull
    private String getTemplateFileHeader(Path relativePath) {
        return TEMPLATE_FILE_HEADER_TAG + "template file " + relativePath.toString() + " **\n";
    }


    private Optional<RenderedTemplate> tryStreamRenderedTemplateContent(Path relativePath, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingMedia renderingMedia) {
        VelocityContext velocityContext = this.createVelocityContext(templateContextObjects, locale, renderingMedia);

        return tryLoadVelocityTemplate(velocityEngine, relativePath)
                .map(template -> this.renderTemplate(template, velocityContext));
    }

    private RenderedTemplate renderTemplate(Template velocityTemplate, VelocityContext velocityContext) {
        try (StringWriter stringWriter = new StringWriter()) {
            velocityTemplate.merge(velocityContext, stringWriter);
            stringWriter.close();

            StringBuffer buffer = stringWriter.getBuffer();
            byte[] templateBytes = buffer.toString().getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(templateBytes);

            TemplateResourcesTool templateResourcesTool = (TemplateResourcesTool) velocityContext.get(RESOURCE_TOOL_CONTEXT_KEY);
            Set<ReferencedResource> referencedResources = templateResourcesTool.getReferencedResources();


            RenderedTemplate renderedTemplate = new RenderedTemplate();
            renderedTemplate.setContentStream(inputStream);
            renderedTemplate.setReferencedResources(referencedResources);
            return renderedTemplate;
        } catch (IOException e) {
            throw new DispatcherRuntimeException("Failed to render template", e);
        }
    }

    private String renderVelocityString(String logTag, String templateValue, VelocityContext velocityContext) {
        if (templateValue == null) {
            return null;
        }
        try {
            StringWriter stringWriter = new StringWriter();
            velocityEngine.evaluate(velocityContext, stringWriter, logTag, templateValue);

            stringWriter.flush();
            return stringWriter.toString();
        } catch (Exception e) {
            throw new DispatcherRuntimeException("Failed to render template string", e);
        }
    }


    private Optional<Template> tryLoadVelocityTemplate(VelocityEngine velocityEngine, Path path) {
        Path relativePath = this.resourceBaseDir
                .map(Paths::get)
                .orElse(Paths.get(""))
                .resolve(path);
        try {
            Template template = velocityEngine.getTemplate(relativePath.toString(), "UTF-8");
            return Optional.of(template);
        } catch (ResourceNotFoundException e) {
            LOG.debug("Velocity template not found at {}", relativePath.toString());
            return Optional.empty();
        } catch (ParseErrorException e) {
            String message = MessageFormat.format("Failed to parse template {0}", relativePath.toString());
            throw new DispatcherRuntimeException(message);
        }
    }

    private VelocityContext createVelocityContext(List<TemplateContextObject> templateContexts, Locale locale, RenderingMedia renderingMedia) {
        VelocityContext context = new VelocityContext();

        templateContexts.stream()
                .forEach(templateContext -> context.put(templateContext.getKey(), templateContext.getValue()));

        DateTool dateTool = new DateTool();
        Map<String, Object> dateToolConfig = new HashMap<>();
        dateToolConfig.put("locale", locale);
        dateTool.configure(dateToolConfig);
        context.put("date", dateTool);

        TemplateResourcesTool templateResourcesTool = new TemplateResourcesTool(renderingMedia, referencedResourceProvider);
        context.put(RESOURCE_TOOL_CONTEXT_KEY, templateResourcesTool);

        return context;
    }

}

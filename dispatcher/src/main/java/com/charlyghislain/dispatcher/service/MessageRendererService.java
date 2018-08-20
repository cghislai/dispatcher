package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.exception.NoTemplateFoundException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.rendering.ReadyToBeRenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailHeaders;
import com.charlyghislain.dispatcher.api.rendering.RenderedMailMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedTemplate;
import com.charlyghislain.dispatcher.api.rendering.RenderingType;
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
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import java.util.concurrent.CompletableFuture;
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
    public RenderedMessage renderMessage(ReadyToBeRenderedMessage readyToBeRenderedMessage) throws DispatcherException {
        Set<DispatchingOption> dispatchingOptions = readyToBeRenderedMessage.getDispatchingOptions();
        Set<DispatchingOption> mailDispatchingOptions = dispatchingOptions.stream()
                .filter(this::isMailDispatching)
                .collect(Collectors.toSet());

        RenderedMessage renderedMessage = new RenderedMessage();

        if (!mailDispatchingOptions.isEmpty()) {
            RenderedMailMessage renderedMailMessage = renderMailMessage(readyToBeRenderedMessage, mailDispatchingOptions);
            renderedMessage.setRenderedMailMessage(renderedMailMessage);
        }

        return renderedMessage;
    }

    @Override
    public InputStream streamNonRenderedTemplate(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption, Locale locale) throws NoTemplateFoundException {
        return messageResourcesService.streamVelocityTemplatePaths(dispatcherMessage, dispatchingOption, locale)
                .map(this::streamRawTemplateContent)
                .findFirst()
                .orElseThrow(() -> new NoTemplateFoundException(dispatcherMessage, dispatchingOption, locale));
    }

    @Override
    public RenderedTemplate renderTemplate(DispatcherMessage message, DispatchingOption dispatchingOption, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingType renderingType) throws DispatcherException {
        Optional<DispatcherMessage> messageHeader = message.getHeader();
        Optional<DispatcherMessage> messageFooter = message.getFooter();

        List<RenderedTemplate> templateList = new ArrayList<>();

        if (messageHeader.isPresent()) {
            RenderedTemplate renderedTemplate = this.renderTemplate(messageHeader.get(), dispatchingOption, locale, templateContextObjects, renderingType);
            templateList.add(renderedTemplate);
        }

        RenderedTemplate renderedContent = streamRenderedTemplatesForLocale(message, locale, dispatchingOption, templateContextObjects, renderingType)
                .findFirst()
                .orElseThrow(() -> new DispatcherException("Could not find any template for message " + message.getName() + ", option " + dispatchingOption + " and locale " + locale));
        templateList.add(renderedContent);

        if (messageFooter.isPresent()) {
            RenderedTemplate renderedTemplate = this.renderTemplate(messageFooter.get(), dispatchingOption, locale, templateContextObjects, renderingType);
            templateList.add(renderedTemplate);
        }

        return RenderedTemplate.compose(templateList.toArray(new RenderedTemplate[0]));
    }

    @Override
    public RenderedMailHeaders renderMailHeaders(MailHeadersTemplate mailHeadersTemplate, Locale locale, List<TemplateContextObject> templateContexts) {
        VelocityContext velocityContext = this.createVelocityContext(templateContexts, locale, RenderingType.MAIL);

        RenderedMailHeaders renderedMailHeaders = new RenderedMailHeaders();
        this.renderSingleAddressHeaderField(mailHeadersTemplate::getFrom, renderedMailHeaders::setFrom, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getTo, renderedMailHeaders::setTo, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getCc, renderedMailHeaders::setCc, velocityContext);
        this.renderMultiAddressesHeaderField(mailHeadersTemplate::getBcc, renderedMailHeaders::setBcc, velocityContext);
        this.renderTextHeaderField(mailHeadersTemplate::getSubject, renderedMailHeaders::setSubject, velocityContext);
        return renderedMailHeaders;
    }

    private RenderedMailMessage renderMailMessage(ReadyToBeRenderedMessage readyToBeRenderedMessage, Set<DispatchingOption> mailDispatchingOptions) throws DispatcherException {
        DispatcherMessage message = readyToBeRenderedMessage.getMessage();
        List<TemplateContextObject> contextObjects = readyToBeRenderedMessage.getContextObjects();
        List<Locale> acceptedLocales = readyToBeRenderedMessage.getAcceptedLocales();
        MailHeadersTemplate mailHeadersTemplate = readyToBeRenderedMessage.getMailHeadersTemplate();
        RenderingType renderingType = readyToBeRenderedMessage.getRenderingType();


        RenderedMailHeaders mailHeaders = acceptedLocales.stream()
                .map(locale -> renderMailHeaders(mailHeadersTemplate, locale, contextObjects))
                .findFirst()
                .orElseThrow(() -> new DispatcherException("Could not render mail headers"));

        Map<DispatchingOption, RenderedTemplate> renderedTemplateMap = new HashMap<>();
        for (DispatchingOption dispatchingOption : mailDispatchingOptions) {
            RenderedTemplate renderedTemplate = acceptedLocales.stream()
                    .map(locale -> tryRenderTemplate(message, dispatchingOption, acceptedLocales, contextObjects, renderingType))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(() -> new DispatcherException("Could not render mail template for dispatching option " + dispatchingOption));
            renderedTemplateMap.put(dispatchingOption, renderedTemplate);
        }

        RenderedMailMessage mailMessage = new RenderedMailMessage();
        mailMessage.setMailMessageHeaders(mailHeaders);
        mailMessage.setRenderedTemplates(renderedTemplateMap);
        return mailMessage;
    }

    private Optional<RenderedTemplate> tryRenderTemplate(DispatcherMessage message, DispatchingOption dispatchingOption, List<Locale> acceptedLocales, List<TemplateContextObject> templateContextObjects, RenderingType renderingType) {
        for (Locale locale : acceptedLocales) {
            try {
                RenderedTemplate renderedTemplate = renderTemplate(message, dispatchingOption, locale, templateContextObjects, renderingType);
                return Optional.of(renderedTemplate);
            } catch (Exception e) {
                // Try next locale
            }
        }
        return Optional.empty();
    }

    private boolean isMailDispatching(DispatchingOption dispatchingOption) {
        return dispatchingOption.equals(DispatchingOption.MAIL_HTML)
                || dispatchingOption.equals(DispatchingOption.MAIL_TEXT);
    }


    private Stream<RenderedTemplate> streamRenderedTemplatesForLocale(DispatcherMessage dispatcherMessage, Locale locale, DispatchingOption dispatchingOption, List<TemplateContextObject> templateContextObjects, RenderingType renderingType) {
        return messageResourcesService.streamVelocityTemplatePaths(dispatcherMessage, dispatchingOption, locale)
                .map(path -> this.tryStreamRenderedTemplateContent(path, locale, templateContextObjects, renderingType))
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


    private Optional<RenderedTemplate> tryStreamRenderedTemplateContent(Path relativePath, Locale locale, List<TemplateContextObject> templateContextObjects, RenderingType renderingType) {
        VelocityContext velocityContext = this.createVelocityContext(templateContextObjects, locale, renderingType);

        return tryLoadVelocityTemplate(velocityEngine, relativePath)
                .map(template -> this.renderTemplate(template, velocityContext));
    }

    private RenderedTemplate renderTemplate(Template velocityTemplate, VelocityContext velocityContext) {
        PipedInputStream pipedInputStream = new PipedInputStream();

        try (PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {
            Writer writer = new OutputStreamWriter(pipedOutputStream);

            RenderedTemplate renderedTemplate = new RenderedTemplate();
            renderedTemplate.setContentStream(pipedInputStream);
            TemplateResourcesTool templateResourcesTool = (TemplateResourcesTool) velocityContext.get(RESOURCE_TOOL_CONTEXT_KEY);

            return CompletableFuture.runAsync(() -> {
                velocityTemplate.merge(velocityContext, writer);
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new DispatcherRuntimeException("Failed to render template", e);
                }
            }).thenApply((a) -> {
                Set<ReferencedResource> referencedResources = templateResourcesTool.getReferencedResources();
                renderedTemplate.setReferencedResources(referencedResources);
                return renderedTemplate;
            }).join();
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

    private VelocityContext createVelocityContext(List<TemplateContextObject> templateContexts, Locale locale, RenderingType renderingType) {
        VelocityContext context = new VelocityContext();

        templateContexts.stream()
                .forEach(templateContext -> context.put(templateContext.getKey(), templateContext.getValue()));

        DateTool dateTool = new DateTool();
        Map<String, Object> dateToolConfig = new HashMap<>();
        dateToolConfig.put("locale", locale);
        dateTool.configure(dateToolConfig);
        context.put("date", dateTool);

        TemplateResourcesTool templateResourcesTool = new TemplateResourcesTool(renderingType, referencedResourceProvider);
        context.put(RESOURCE_TOOL_CONTEXT_KEY, templateResourcesTool);

        return context;
    }

}

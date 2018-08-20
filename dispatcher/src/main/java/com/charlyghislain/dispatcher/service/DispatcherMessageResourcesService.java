package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.exception.NoMailHeadersTemplateFoundException;
import com.charlyghislain.dispatcher.api.filter.DispatcherMessageFilter;
import com.charlyghislain.dispatcher.api.header.MailHeaders;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.Message;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.mail.ReferencedResourceProvider;
import com.charlyghislain.dispatcher.util.FieldAccessUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class DispatcherMessageResourcesService implements MessageResourcesService, ReferencedResourceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherMessageResourcesService.class);
    private static final String VELOCITY_TEMPLATE_EXTENSION = ".vm";
    private static final String PROPERTIES_EXTENSION = ".properties";

    @Inject
    @Message
    private Instance<DispatcherMessage> messages;

    @Inject
    @ConfigProperty(name = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH,
            defaultValue = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH_DEFAULT_VALUE)
    private Optional<String> filesystemResourcePath;
    @Inject
    @ConfigProperty(name = ConfigConstants.RESOURCES_BASE_DIR,
            defaultValue = ConfigConstants.RESOURCES_BASE_DIR_DEFAULT_VALUE)
    private Optional<String> resourcesBaseDir;
    @Inject
    @ConfigProperty(name = ConfigConstants.WEB_ACCESSIBLE_RESOURCES_URL,
            defaultValue = ConfigConstants.WEB_ACCESSIBLE_RESOURCES_URL_DEFAULT_VALUE)
    private Optional<String> webAccessibleResourceUrl;
    @Inject
    @ConfigProperty(name = ConfigConstants.SHARED_RESOURCES_PATH,
            defaultValue = ConfigConstants.SHARED_RESOURCES_PATH_DEFAULT_VALUE)
    private Optional<String> sharedResourcesPath;

    private Optional<ClassLoader> filesystemResourcesClassLoaderOptional;
    private ClassLoader classpathResourcesClassloader;
    private Map<String, ReferencedResource> referencedResources;

    @PostConstruct
    public void init() {
        filesystemResourcesClassLoaderOptional = this.getFilesystemResourcesClassLoader();
        classpathResourcesClassloader = this.getClasspathResourcesClassLoader();
        referencedResources = new ConcurrentSkipListMap<>();
    }


    @Override
    public Optional<DispatcherMessage> findMessageByName(String name) {
        return messages.stream()
                .filter(m -> m.getName().equals(name))
                .findAny();
    }

    @Override
    public List<DispatcherMessage> findMessages(DispatcherMessageFilter filter) {
        return messages.stream()
                .filter(m -> this.filterMessage(m, filter))
                .collect(Collectors.toList());
    }


    @Override
    public MailHeadersTemplate findMailMessageHeaders(DispatcherMessage message, Locale locale) throws NoMailHeadersTemplateFoundException {
        MailHeadersTemplate headersTemplate = new MailHeadersTemplate();

        this.getMailHeaderTemplateValue(message, locale, "from")
                .ifPresent(headersTemplate::setFrom);
        this.getMailHeaderTemplateValue(message, locale, "to")
                .ifPresent(headersTemplate::setTo);
        this.getMailHeaderTemplateValue(message, locale, "cc")
                .ifPresent(headersTemplate::setCc);
        this.getMailHeaderTemplateValue(message, locale, "bcc")
                .ifPresent(headersTemplate::setBcc);
        this.getMailHeaderTemplateValue(message, locale, "subject")
                .ifPresent(headersTemplate::setSubject);

        if (headersTemplate.isEmpty()) {
            throw new NoMailHeadersTemplateFoundException(message, locale);
        }
        return headersTemplate;
    }

    @Override
    public Stream<ResourceBundle> findMailHeadersResourceBundles(DispatcherMessage dispatcherMessage, Locale locale) {
        String bundleName = this.getMailHeadersBundleBaseName(dispatcherMessage);
        ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);

        List<ResourceBundle> bundles = new ArrayList<>();
        filesystemResourcesClassLoaderOptional
                .flatMap(classLoader -> loadBundle(locale, bundleName, classLoader, control))
                .ifPresent(bundles::add);
        loadBundle(locale, bundleName, classpathResourcesClassloader, control)
                .ifPresent(bundles::add);
        return bundles.stream();
    }

    @Override
    public Stream<Path> streamMailHeadersPropertiesBundlePaths(DispatcherMessage dispatcherMessage, Locale locale) {
        return this.streamCandidateLocaleSuffixes(locale)
                .map(suffix -> this.getMailHeadersPropertiesBundleFilePath(dispatcherMessage, suffix));
    }


    @Override
    public Optional<ReferencedResource> findReferencedResource(String resourceId) {
        ReferencedResource referencedResource = this.referencedResources.get(resourceId);
        return Optional.ofNullable(referencedResource);
    }

    @Override
    public InputStream streamReferencedResource(ReferencedResource referencedResource) {
        Path relativePath = referencedResource.getRelativePath();
        return this.streamResource(relativePath);
    }

    @Override
    public Stream<Path> streamAllSharedResourcesFilePaths() {
        return streamExistingSharedResourcesFilePaths();
    }

    @Override
    public boolean doesSharedResourceExists(Path relativePath) {
        String resourcePathName = relativePath.toString();
        boolean existsOnFilesystem = filesystemResourcesClassLoaderOptional
                .map(classLoader -> classLoader.getResource(resourcePathName))
                .isPresent();
        if (existsOnFilesystem) {
            return true;
        }
        URL classPathResourceUrl = this.classpathResourcesClassloader.getResource(resourcePathName);
        if (classPathResourceUrl != null) {
            return true;
        }
        return false;
    }

    @Override
    public String getSharedResourceMimeType(Path resourcePath) {
        return this.guessMimeType(resourcePath);
    }

    @Override
    public InputStream streamSharedResource(Path resourcePath) {
        return this.streamResource(resourcePath);
    }


    @Override
    public Stream<Path> streamVelocityTemplatePaths(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption, Locale locale) {
        return this.streamCandidateLocaleSuffixes(locale)
                .map(suffix -> this.getVelocityTemplateRelativePath(dispatcherMessage, dispatchingOption, suffix));
    }

    @Override
    public InputStream streamVelocityTemplate(Path templatePath) {
        return this.streamResource(templatePath);
    }

    @Override
    public Stream<Locale> streamLocalesWithExistingVelocityTemplateOrResourceBundle(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption) {
        Path folderRelativePath = this.getMessageFolderPath(dispatcherMessage);

        Stream<Path> localesWithTemplateStream = this.streamExistingVelocityTemplateFilePaths(folderRelativePath, dispatchingOption);
        Stream<Path> localesWithResourceBundle = this.streamExistingResourceBundleFilePaths(folderRelativePath, dispatchingOption);
        return Stream.concat(localesWithResourceBundle, localesWithTemplateStream)
                .map(this::resolveLocalizedFileLocale)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct();
    }

    @Override
    public void clearResourceCaches() {
        filesystemResourcesClassLoaderOptional
                .ifPresent(ResourceBundle::clearCache);
        this.referencedResources.clear();
    }


    @Override
    public ReferencedResource findReferencedResourceForPath(Path path) throws DispatcherException {
        Path normalizedPath = getSharedResourcesPath()
                .resolve(path)
                .normalize();

        this.checkSharedResourceExists(normalizedPath);

        return referencedResources.values().stream()
                .filter(e -> isSameReferencedResource(normalizedPath, e))
                .findAny()
                .orElseGet(() -> this.createNewReferencedResource(normalizedPath));
    }

    @Override
    public URI getWebUrl(ReferencedResource referencedResource) throws DispatcherException {
        return this.webAccessibleResourceUrl
                .map(this::getNonEmptyString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(url -> url + "?resourceId=" + referencedResource.getId())
                .map(URI::create)
                .orElseThrow(() -> new DispatcherException("No web accessible url configured"));
    }


    private Optional<ResourceBundle> loadBundle(Locale locale, String bundleName, ClassLoader classLoader, ResourceBundle.Control control) {
        try {
            ResourceBundle resourcesBundle = ResourceBundle.getBundle(bundleName, locale, classLoader, control);
            return Optional.of(resourcesBundle);
        } catch (MissingResourceException e) {
            LOG.debug("No bundle found for name {} and locale {}", bundleName, locale.toLanguageTag());
            return Optional.empty();
        }
    }

    private Stream<String> streamMailHeaderConfigurationKeys(DispatcherMessage dispatcherMessage, String headerName, Locale locale) {
        Stream<String> messageLevelKeys = this.streamCandidateLocaleSuffixes(locale)
                .map(suffix -> this.getMailHeaderConfigurationKey(dispatcherMessage, headerName, suffix));
        Stream<String> genericHeadersKeys = this.streamCandidateLocaleSuffixes(locale)
                .map(suffix -> this.getGenericMailHeaderConfigurationKey(headerName, suffix));
        return Stream.concat(messageLevelKeys, genericHeadersKeys);
    }

    private Stream<String> streamCandidateLocaleSuffixes(Locale locale) {
        ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        return control.getCandidateLocales("", locale)
                .stream()
                .map(candidate -> control.toBundleName("", candidate));
    }

    private String getMailHeadersBundleBaseName(DispatcherMessage dispatcherMessage) {
        String messageName = dispatcherMessage.getName();
        String bundleName = MailHeaders.class.getSimpleName();
        return messageName + "." + bundleName;
    }

    private Path getMailHeadersPropertiesBundleFilePath(DispatcherMessage dispatcherMessage, String localeSuffix) {
        String messageName = dispatcherMessage.getName();
        String mailHeadersName = MailHeaders.class.getSimpleName();
        String bundleName = mailHeadersName + localeSuffix + PROPERTIES_EXTENSION;
        return Paths.get(messageName, bundleName);
    }

    private String getMailHeaderConfigurationKey(DispatcherMessage dispatcherMessage, String headerName, String localeSuffix) {
        String qualifedName = dispatcherMessage.getQualifiedName();
        String mailHeadersName = MailHeaders.class.getSimpleName();
        String configKeySuffix = getNonEmptyString(localeSuffix)
                .orElse("");
        return qualifedName + "." + mailHeadersName + "#" + headerName + configKeySuffix;
    }

    private String getGenericMailHeaderConfigurationKey(String headerName, String localeSuffix) {
        String qualifiedHeadersName = MailHeaders.class.getName();
        String configKeySuffix = getNonEmptyString(localeSuffix)
                .orElse("");
        return qualifiedHeadersName + "#" + headerName + configKeySuffix;
    }


    private Path getVelocityTemplateRelativePath(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption, String localeSuffix) {
        Path folder = this.getMessageFolderPath(dispatcherMessage);
        String velocityTemplateFileName = this.getVelocityTemplateFileName(dispatchingOption, localeSuffix);
        return folder.resolve(velocityTemplateFileName);
    }

    private Path getMessageFolderPath(DispatcherMessage dispatcherMessage) {
        String name = dispatcherMessage.getName();
        return Paths.get(name);
    }

    private String getVelocityTemplateFileName(DispatchingOption dispatchingOption, String localeSuffix) {
        String disptachName = dispatchingOption.getTemplateFileName();
        return disptachName + localeSuffix + VELOCITY_TEMPLATE_EXTENSION;
    }

    private Optional<Locale> resolveLocalizedFileLocale(Path path) {
        String fileName = path.getFileName().toString();
        int firstUnderscoreIndex = fileName.indexOf("_");
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex < 0 || firstUnderscoreIndex >= lastDotIndex) {
            return Optional.empty();
        }
        if (firstUnderscoreIndex < 0) {
            return Optional.of(Locale.ROOT);
        }
        String languagePart = fileName.substring(firstUnderscoreIndex + 1, lastDotIndex);
        String languageTag = languagePart.replaceAll("_", "-");
        if (languageTag.isEmpty()) {
            return Optional.of(Locale.ROOT);
        }
        try {
            Locale locale = Locale.forLanguageTag(languageTag);
            return Optional.of(locale);
        } catch (Exception e) {
            LOG.warn("Could not parse locale for template file " + fileName, e);
            return Optional.empty();
        }
    }

    private Stream<Path> streamExistingVelocityTemplateFilePaths(Path messageFolderPath, DispatchingOption dispatchingOption) {
        String messageFolderPathName = messageFolderPath.toString();

        Stream<Path> filesystemTemplatePaths = filesystemResourcesClassLoaderOptional
                .map(classLoader -> classLoader.getResource(messageFolderPathName))
                .map(url -> this.streamExistingVelocityTemplateFilePaths(url, dispatchingOption))
                .orElse(Stream.empty());

        URL classPathResourceUrl = this.classpathResourcesClassloader.getResource(messageFolderPathName);
        Stream<Path> classPathFiles = Optional.ofNullable(classPathResourceUrl)
                .map(url -> this.streamExistingVelocityTemplateFilePaths(url, dispatchingOption))
                .orElse(Stream.empty());

        return Stream.concat(filesystemTemplatePaths, classPathFiles)
                .distinct();
    }

    private Stream<Path> streamExistingVelocityTemplateFilePaths(URL folderPathUrl, DispatchingOption dispatchingOption) {
        try {
            URI folderPathURI = folderPathUrl.toURI();
            Path folderPath = Paths.get(folderPathURI);
            return Files.list(folderPath)
                    .filter(path -> this.isVelocityTemplate(path, dispatchingOption));
        } catch (URISyntaxException | IOException e) {
            throw new DispatcherRuntimeException("Failed to list message folder content at " + folderPathUrl);
        }
    }

    private Stream<Path> streamExistingResourceBundleFilePaths(Path messageFolderPath, DispatchingOption dispatchingOption) {
        String messageFolderPathName = messageFolderPath.toString();

        Stream<Path> filesystemBundlePaths = filesystemResourcesClassLoaderOptional
                .map(classLoader -> classLoader.getResource(messageFolderPathName))
                .map(url -> this.streamExistingResourceBundleFilePaths(url, dispatchingOption))
                .orElse(Stream.empty());

        URL classPathResourceUrl = this.classpathResourcesClassloader.getResource(messageFolderPathName);
        Stream<Path> classPathFiles = Optional.ofNullable(classPathResourceUrl)
                .map(url -> this.streamExistingResourceBundleFilePaths(url, dispatchingOption))
                .orElse(Stream.empty());

        return Stream.concat(filesystemBundlePaths, classPathFiles)
                .distinct();
    }

    private Stream<Path> streamExistingResourceBundleFilePaths(URL folderPathUrl, DispatchingOption dispatchingOption) {
        try {
            URI folderPathURI = folderPathUrl.toURI();
            Path folderPath = Paths.get(folderPathURI);
            return Files.list(folderPath)
                    .filter(path -> this.isResourceBundle(path, dispatchingOption));
        } catch (URISyntaxException | IOException e) {
            throw new DispatcherRuntimeException("Failed to list message folder content at " + folderPathUrl);
        }
    }


    private Stream<Path> streamExistingSharedResourcesFilePaths() {
        String sharedResourcesPathName = getSharedResourcesPath().toString();

        Stream<Path> filesystemFiles = filesystemResourcesClassLoaderOptional
                .map(classLoader -> classLoader.getResource(sharedResourcesPathName))
                .map(this::streamExistingSharedResourcesFilePaths)
                .orElse(Stream.empty());

        URL classPathResourceUrl = this.classpathResourcesClassloader.getResource(sharedResourcesPathName);
        Stream<Path> classPathFiles = Optional.ofNullable(classPathResourceUrl)
                .map(this::streamExistingSharedResourcesFilePaths)
                .orElse(Stream.empty());

        return Stream.concat(filesystemFiles, classPathFiles)
                .distinct();
    }

    private Stream<Path> streamExistingSharedResourcesFilePaths(URL folderPathUrl) {
        try {
            URI folderPathURI = folderPathUrl.toURI();
            Path folderPath = Paths.get(folderPathURI);
            return Files.walk(folderPath)
                    .filter(Files::isRegularFile);
        } catch (URISyntaxException | IOException e) {
            throw new DispatcherRuntimeException("Failed to list message folder content at " + folderPathUrl);
        }
    }


    private boolean isVelocityTemplate(Path path, DispatchingOption dispatchingOption) {
        String fileName = path.getFileName().toString();
        return fileName.startsWith(dispatchingOption.getTemplateFileName())
                && fileName.endsWith(VELOCITY_TEMPLATE_EXTENSION);
    }

    private boolean isResourceBundle(Path path, DispatchingOption dispatchingOption) {
        return this.streamDispatchingOptionBundleBaseNames(dispatchingOption)
                .anyMatch(bundleName -> this.hasBundleName(path, bundleName));
    }

    private boolean hasBundleName(Path path, String bundleName) {
        String fileName = path.getFileName().toString();
        return fileName.startsWith(bundleName)
                && fileName.endsWith(PROPERTIES_EXTENSION);

    }

    private Stream<String> streamDispatchingOptionBundleBaseNames(DispatchingOption dispatchingOption) {
        switch (dispatchingOption) {
            case MAIL_HTML:
            case MAIL_TEXT:
                return Stream.of(MailHeaders.class.getSimpleName());
        }
        return Stream.empty();
    }

    private Optional<ClassLoader> getFilesystemResourcesClassLoader() {
        Path resourcesBaseDirPath = this.getResourcesBaseDirPath();
        Optional<URL> filesystemResourcesUrl = this.filesystemResourcePath
                .flatMap(this::getNonEmptyString)
                .map(Paths::get)
                .map(path -> path.resolve(resourcesBaseDirPath))
                .map(Path::toUri)
                .map(this::parseUri);
        filesystemResourcesUrl.ifPresent(this::logEnabledClassLoader);
        return filesystemResourcesUrl.map(url -> new URL[]{url})
                .map(URLClassLoader::new);
    }

    private void logEnabledClassLoader(URL url) {
        LOG.info("Enabling class loader for message resources at " + url.toString());
    }

    private URL parseUri(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new DispatcherRuntimeException("Failed to parse uri to url: " + uri, e);
        }
    }

    private ClassLoader getClasspathResourcesClassLoader() {
        String resourceBaseDirName = getResourcesBaseDirPath().toString();
        ClassLoader classLoader = getClass().getClassLoader();
        URL classLoaderUrl = Optional.ofNullable(classLoader.getResource(resourceBaseDirName))
                .orElseThrow(() -> new DispatcherRuntimeException("Failed to instantiate classpath class loader"));

        this.logEnabledClassLoader(classLoaderUrl);
        return new URLClassLoader(new URL[]{classLoaderUrl});
    }


    private Optional<String> getNonEmptyString(String value) {
        if (value.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value);
    }


    private Optional<String> getMailHeaderTemplateValue(DispatcherMessage dispatcherMessage, Locale locale, String headerName) {
        String value;

        // priority 1: properties
        Config config = ConfigProvider.getConfig();
        value = this.streamMailHeaderConfigurationKeys(dispatcherMessage, headerName, locale)
                .map(key -> config.getOptionalValue(key, String.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .flatMap(this::getNonEmptyString)
                .orElse(null);

        // priority 2: resource bundle
        if (value == null) {
            try {
                value = this.findMailHeadersResourceBundles(dispatcherMessage, locale)
                        .map(bundle -> bundle.getString(headerName))
                        .map(this::getNonEmptyString)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(this::reencodeResourceBundleValueAsUtf8)
                        .findFirst()
                        .orElse(null);
            } catch (MissingResourceException e) {
                LOG.debug("Resource bundle for message {} and locale {} is missing the key {}",
                        dispatcherMessage.getQualifiedName(), locale.toLanguageTag(), headerName);
                value = null;
            }
        }

        // priority 3: MailHeader annotation
        if (value == null) {
            value = this.getMailHeaderTemplateAnnotationValue(dispatcherMessage, headerName)
                    .orElse(null);
        }

        // No header value -> exception if required
        boolean required = this.isMailHeaderRequired(headerName);
        if (required && value == null) {
            String message = MessageFormat.format("MessageDefinition {0} is missing mail header {1}",
                    dispatcherMessage.getQualifiedName(), headerName);
            throw new DispatcherRuntimeException(message);
        }

        return Optional.ofNullable(value);
    }

    private String reencodeResourceBundleValueAsUtf8(String iso1Value) {
        try {
            return new String(iso1Value.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DispatcherRuntimeException(e);
        }
    }

    private Optional<String> getMailHeaderTemplateAnnotationValue(DispatcherMessage dispatcherMessage, String headerName) {
        Class<?> messageType = dispatcherMessage.getMessageType();
        MailHeaders mailheaders = messageType.getAnnotation(MailHeaders.class);
        if (mailheaders == null) {
            return Optional.empty();
        }

        Method headerMethod = FieldAccessUtils.getMethodNamed(headerName, MailHeaders.class);
        String value = (String) FieldAccessUtils.invokeGetter(headerMethod, mailheaders);
        return this.getNonEmptyString(value);
    }

    private boolean isMailHeaderRequired(String headerName) {
        Field field = FieldAccessUtils.getFieldNamed(headerName, MailHeadersTemplate.class);
        return Optional.ofNullable(field.getAnnotation(NotNull.class)).isPresent();
    }


    private ReferencedResource createNewReferencedResource(Path path) {
        String newId = UUID.randomUUID().toString();
        String mimeType = this.guessMimeType(path);

        ReferencedResource referencedResource = new ReferencedResource();
        referencedResource.setId(newId);
        referencedResource.setRelativePath(path);
        referencedResource.setMimeType(mimeType);
        referencedResources.put(newId, referencedResource);
        return referencedResource;
    }

    private String guessMimeType(Path resourcePath) {
        InputStream inputStream = this.streamResource(resourcePath);
        if (inputStream != null) {
            try {
                String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
                inputStream.close();
                return mimeType;
            } catch (IOException e) {
                throw new DispatcherRuntimeException(e);
            }
        } else {
            throw new DispatcherRuntimeException("Could not access resource " + resourcePath.toString());
        }
    }

    private boolean isSameReferencedResource(Path normalizedPath, ReferencedResource referencedResource) {
        return referencedResource.getRelativePath().equals(normalizedPath);
    }

    private InputStream streamResource(Path relativePath) {
        String resourcePathName = relativePath.toString();

        Optional<InputStream> filesystemResourceStream = this.filesystemResourcesClassLoaderOptional
                .map(classLoader -> classLoader.getResourceAsStream(resourcePathName));
        if (filesystemResourceStream.isPresent()) {
            return filesystemResourceStream.get();
        }

        InputStream classPathResourceStream = this.classpathResourcesClassloader.getResourceAsStream(resourcePathName);
        if (classPathResourceStream != null) {
            return classPathResourceStream;
        }
        throw new DispatcherRuntimeException("Resource not found: " + resourcePathName);
    }


    private void checkSharedResourceExists(Path resourcePath) throws DispatcherException {
        boolean exists = this.doesSharedResourceExists(resourcePath);
        if (!exists) {
            throw new DispatcherException("Resource does not exist: " + resourcePath);
        }
    }

    private Path getSharedResourcesPath() {
        return sharedResourcesPath.map(Paths::get)
                .orElse(Paths.get(""));
    }

    private Path getResourcesBaseDirPath() {
        return resourcesBaseDir.map(Paths::get)
                .orElse(Paths.get(""));
    }


    private boolean filterMessage(DispatcherMessage m, DispatcherMessageFilter filter) {
        // TODO
        return true;
    }

}

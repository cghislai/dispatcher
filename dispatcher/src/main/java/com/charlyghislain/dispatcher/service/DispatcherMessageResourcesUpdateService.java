package com.charlyghislain.dispatcher.service;

import com.charlyghislain.dispatcher.api.configuration.ConfigConstants;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.DispatcherRuntimeException;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.api.service.MessageResourcesUpdateService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

@ApplicationScoped
public class DispatcherMessageResourcesUpdateService implements MessageResourcesUpdateService {

    @Inject
    private MessageResourcesService messageResourcesService;

    @Inject
    @ConfigProperty(name = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH,
            defaultValue = ConfigConstants.FILESYSTEM_WRITABLE_RESOURCE_PATH_DEFAULT_VALUE)
    private Optional<String> fileSystemResourcePath;
    @Inject
    @ConfigProperty(name = ConfigConstants.RESOURCES_BASE_DIR,
            defaultValue = ConfigConstants.RESOURCES_BASE_DIR_DEFAULT_VALUE)
    private Optional<String> resourceBaseDir;
    @Inject
    @ConfigProperty(name = ConfigConstants.SHARED_RESOURCES_PATH,
            defaultValue = ConfigConstants.SHARED_RESOURCES_PATH_DEFAULT_VALUE)
    private String sharedResourcesPath;


    @Override
    public void setMessageTemplateContent(DispatcherMessage message, DispatchingOption dispatchingOption, Locale locale, InputStream contentStream) {

        Path relativePath = messageResourcesService.streamVelocityTemplatePaths(message, dispatchingOption, locale)
                .findFirst()
                .orElseThrow(() -> new DispatcherRuntimeException("Failed to find a template path"));

        Path absolutePath = getFilesystemResourcesPath()
                .resolve(relativePath)
                .toAbsolutePath();

        writeFileContent(contentStream, absolutePath);
    }

    @Override
    public void setMailHeadersTemplate(DispatcherMessage message, Locale locale, MailHeadersTemplate mailHeaders) {
        Path relativePath = messageResourcesService.streamMailHeadersPropertiesBundlePaths(message, locale)
                .findFirst()
                .orElseThrow(() -> new DispatcherRuntimeException("Failed to find a bundle file path"));

        Path absolutePath = getFilesystemResourcesPath()
                .resolve(relativePath)
                .toAbsolutePath();

        writePropertiesContent(mailHeaders, absolutePath);
        this.messageResourcesService.clearResourceCaches();
    }

    @Override
    public void uploadResource(Path resourcePath, String mimeType, InputStream contentStream) {
        Path absolutePath = getFilesystemResourcesPath()
                .resolve(sharedResourcesPath)
                .resolve(resourcePath)
                .toAbsolutePath();

        writeFileContent(contentStream, absolutePath);
    }

    private void writePropertiesContent(MailHeadersTemplate mailHeaders, Path absolutePath) {
        Properties properties = new Properties();
        if (Files.exists(absolutePath)) {
            try (InputStream inputStream = Files.newInputStream(absolutePath)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new DispatcherRuntimeException(e);
            }
        }

        try {
            Files.createDirectories(absolutePath.getParent());
        } catch (IOException e) {
            throw new DispatcherRuntimeException(e);
        }

        this.fillMailHeadersProperties(properties, mailHeaders);

        try (OutputStream outputStream = Files.newOutputStream(absolutePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            properties.store(outputStream, "Headers updated");
        } catch (IOException e) {
            throw new DispatcherRuntimeException("Failed to open file " + absolutePath.getFileName());
        }
    }

    private void writeFileContent(InputStream contentStream, Path absolutePath) {
        OutputStream outputStream;
        try {
            Files.createDirectories(absolutePath.getParent());
            outputStream = Files.newOutputStream(absolutePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            InputStreamReader streamReader = new InputStreamReader(contentStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            bufferedReader.lines()
                    .filter(line -> !line.startsWith(MessageRendererService.TEMPLATE_FILE_HEADER_TAG))
                    .map(line -> line + "\n")
                    .map(line -> line.getBytes(StandardCharsets.UTF_8))
                    .forEach(bytes -> writeBytes(outputStream, bytes));
            bufferedReader.close();
            outputStream.close();
        } catch (IOException e) {
            throw new DispatcherRuntimeException("Failed to open file " + absolutePath.getFileName());
        }
    }

    private void fillMailHeadersProperties(Properties properties, MailHeadersTemplate mailHeaders) {
        setPropertiesValue(properties, "from", mailHeaders.getFrom());
        setPropertiesValue(properties, "to", mailHeaders.getTo());
        setPropertiesValue(properties, "cc", mailHeaders.getCc());
        setPropertiesValue(properties, "bcc", mailHeaders.getBcc());
        setPropertiesValue(properties, "subject", mailHeaders.getSubject());
    }

    private void setPropertiesValue(Properties properties, String name, String value) {
        properties.remove(name);
        if (value != null) {
            properties.put(name, value);
        }
    }

    private void writeBytes(OutputStream outputStream, byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new DispatcherRuntimeException(e);
        }
    }


    private Path getFilesystemResourcesPath() {
        Path filesystemResourcesPath = fileSystemResourcePath.filter(path -> !path.trim().isEmpty())
                .map(Paths::get)
                .orElseThrow(() -> new DispatcherRuntimeException("Filesystem resources path not set in configuration"));
        Path baseDirPath = resourceBaseDir.map(Paths::get)
                .orElse(Paths.get(""));
        return filesystemResourcesPath.resolve(baseDirPath);
    }


}

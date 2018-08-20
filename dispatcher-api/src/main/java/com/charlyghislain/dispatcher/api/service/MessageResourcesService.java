package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.exception.NoMailHeadersTemplateFoundException;
import com.charlyghislain.dispatcher.api.exception.SharedResourceNotFoundException;
import com.charlyghislain.dispatcher.api.filter.DispatcherMessageFilter;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public interface MessageResourcesService {

    Optional<DispatcherMessage> findMessageByName(String name);

    List<DispatcherMessage> findMessages(DispatcherMessageFilter filter);


    MailHeadersTemplate findMailMessageHeaders(DispatcherMessage dispatcherMessage, Locale locale) throws NoMailHeadersTemplateFoundException;

    Stream<ResourceBundle> findMailHeadersResourceBundles(DispatcherMessage dispatcherMessage, Locale locale);

    Stream<Path> streamMailHeadersPropertiesBundlePaths(DispatcherMessage dispatcherMessage, Locale locale);


    Optional<ReferencedResource> findReferencedResource(String resourceId);

    InputStream streamReferencedResource(ReferencedResource referencedResource);


    Stream<Path> streamAllSharedResourcesFilePaths();

    boolean doesSharedResourceExists(Path relativePath);

    String getSharedResourceMimeType(Path resourcePath) throws SharedResourceNotFoundException;

    InputStream streamSharedResource(Path resourcePath) throws SharedResourceNotFoundException;


    Stream<Path> streamVelocityTemplatePaths(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption, Locale locale);

    InputStream streamVelocityTemplate(Path templatePath);

    Stream<Locale> streamLocalesWithExistingVelocityTemplateOrResourceBundle(DispatcherMessage dispatcherMessage, DispatchingOption dispatchingOption);


    void clearResourceCaches();
}

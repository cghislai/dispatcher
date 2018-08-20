package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

public interface MessageResourcesUpdateService {

    void setMessageTemplateContent(DispatcherMessage message, DispatchingOption dispatchingOption, Locale locale, InputStream contentStream);

    void setMailHeadersTemplate(DispatcherMessage message, Locale locale, MailHeadersTemplate mailHeaders);

    void uploadResource(Path resourcePath, String mimeType, InputStream contentStream);

}

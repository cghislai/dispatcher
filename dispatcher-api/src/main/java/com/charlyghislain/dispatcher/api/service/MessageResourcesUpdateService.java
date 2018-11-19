package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

public interface MessageResourcesUpdateService {

    void setMessageTemplateContent(DispatcherMessage message, RenderingOption renderingOption, Locale locale, InputStream contentStream);

    void setMailHeadersTemplate(DispatcherMessage message, Locale locale, MailHeadersTemplate mailHeaders);

    void uploadResource(Path resourcePath, String mimeType, InputStream contentStream);

}

package com.charlyghislain.dispatcher.management.service;

import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ApplicationScoped
public class LocalesService {

    public List<Locale> getAcceptedLanguages(HttpHeaders httpHeaders) {
        try {
            return httpHeaders.getAcceptableLanguages()
                    .stream()
                    .map(this::getAcceptedLocale)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            String headerValue = httpHeaders.getHeaderString("Accept-Language");
            throw new DispatcherWebException(DispatcherWebError.INVALID_LANGUAGE, "Provided language is invalid: " + headerValue);
        }
    }

    public Locale getLocale(String languageTag) {
        try {
            return Locale.forLanguageTag(languageTag);
        } catch (Exception e) {
            throw new DispatcherWebException(DispatcherWebError.INVALID_LANGUAGE, "Provided language is invalid: " + languageTag);
        }
    }

    private Locale getAcceptedLocale(Locale locale) {
        String language = locale.getLanguage();
        if (language.trim().isEmpty() || language.equals("*")) {
            return Locale.ROOT;
        }
        return locale;
    }
}

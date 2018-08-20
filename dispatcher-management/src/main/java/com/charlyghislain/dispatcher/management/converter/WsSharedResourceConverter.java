package com.charlyghislain.dispatcher.management.converter;


import com.charlyghislain.dispatcher.api.exception.SharedResourceNotFoundException;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.management.api.domain.WsSharedResource;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;

@ApplicationScoped
public class WsSharedResourceConverter {

    @Inject
    private MessageResourcesService messageResourcesService;

    public WsSharedResource toWsSharedResource(Path resourcePath) {
        try {
            String mimeType = messageResourcesService.getSharedResourceMimeType(resourcePath);
            String fileName = resourcePath.getFileName().toString();

            WsSharedResource wsCommunicationSharedResource = new WsSharedResource();
            wsCommunicationSharedResource.setFileName(fileName);
            wsCommunicationSharedResource.setMimeType(mimeType);
            wsCommunicationSharedResource.setRelativePath(resourcePath.toString());
            return wsCommunicationSharedResource;
        } catch (SharedResourceNotFoundException e) {
            throw new DispatcherWebException(DispatcherWebError.RESOURCE_NOT_FOUND);
        }
    }
}

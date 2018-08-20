package com.charlyghislain.dispatcher.management.web;

import com.charlyghislain.dispatcher.api.exception.SharedResourceNotFoundException;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.api.service.MessageResourcesUpdateService;
import com.charlyghislain.dispatcher.management.api.DispatcherSharedResourcesResource;
import com.charlyghislain.dispatcher.management.api.domain.WsResultList;
import com.charlyghislain.dispatcher.management.api.domain.WsSharedResource;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.converter.WsSharedResourceConverter;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;
import com.charlyghislain.dispatcher.management.service.StreamingService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.charlyghislain.dispatcher.management.api.security.DispatcherManagementRoles.ROLE_MANAGER;

@RolesAllowed(ROLE_MANAGER)
public class DispatcherSharedResourcesResourceController implements DispatcherSharedResourcesResource {

    @Inject
    private WsSharedResourceConverter wsSharedResourceConverter;

    @Inject
    private MessageResourcesService messageResourcesService;
    @Inject
    private MessageResourcesUpdateService messageResourcesUpdateService;

    @Inject
    private StreamingService streamingService;

    @Override
    public WsResultList<WsSharedResource> listAllSharedResources() {
        List<WsSharedResource> sharedResources = messageResourcesService.streamAllSharedResourcesFilePaths()
                .map(wsSharedResourceConverter::toWsSharedResource)
                .collect(Collectors.toList());

        return new WsResultList<>(sharedResources, sharedResources.size());
    }

    @Override
    public WsSharedResource getSharedResource(String resourcePath) {
        Path path = Paths.get(resourcePath);
        boolean resourceExists = messageResourcesService.doesSharedResourceExists(path);
        if (resourceExists) {
            return wsSharedResourceConverter.toWsSharedResource(path);
        } else {
            throw new DispatcherWebException(DispatcherWebError.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void uploadSharedResource(String resourcePath, String contentType, InputStream contentStream) {
        Path path = Paths.get(resourcePath);
        messageResourcesUpdateService.uploadResource(path, contentType, contentStream);
    }

    @Override
    public StreamingOutput streamSharedResource(String resourcePath, HttpServletResponse servletResponse) {
        Path path = Paths.get(resourcePath);
        try {
            String mimeType = messageResourcesService.getSharedResourceMimeType(path);
            InputStream resourceContent = messageResourcesService.streamSharedResource(path);

            servletResponse.setHeader("Content-Type", mimeType);
            return streamingService.streamOutput(resourceContent);
        } catch (SharedResourceNotFoundException e) {
            throw new DispatcherWebException(DispatcherWebError.RESOURCE_NOT_FOUND);
        }
    }
}

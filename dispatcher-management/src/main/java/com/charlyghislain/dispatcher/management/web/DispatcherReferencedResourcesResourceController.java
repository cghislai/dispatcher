package com.charlyghislain.dispatcher.management.web;

import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.service.MessageResourcesService;
import com.charlyghislain.dispatcher.management.api.DispatcherReferencedResourcesResource;
import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import com.charlyghislain.dispatcher.management.error.DispatcherWebException;
import com.charlyghislain.dispatcher.management.service.StreamingService;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

@PermitAll
@ApplicationScoped
public class DispatcherReferencedResourcesResourceController implements DispatcherReferencedResourcesResource {

    @Inject
    private MessageResourcesService messageResourcesService;

    @Inject
    private StreamingService streamingService;


    @Override
    public StreamingOutput streamReferencedResource(String resourceId, HttpServletResponse servletResponse) {
        ReferencedResource referencedResource = messageResourcesService.findReferencedResource(resourceId)
                .orElseThrow(() -> new DispatcherWebException(DispatcherWebError.RESOURCE_NOT_FOUND));
        String mimeType = referencedResource.getMimeType();
        InputStream resourceContent = messageResourcesService.streamReferencedResource(referencedResource);

        servletResponse.setHeader("Content-Type", mimeType);
        return streamingService.streamOutput(resourceContent);
    }
}

package com.charlyghislain.dispatcher.util;

import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;
import com.charlyghislain.dispatcher.api.rendering.RenderingMedia;
import com.charlyghislain.dispatcher.mail.ReferencedResourceProvider;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.FormatConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@DefaultKey("res")
public class TemplateResourcesTool extends FormatConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateResourcesTool.class);
    private final RenderingMedia renderingMedia;
    private final ReferencedResourceProvider referencedResourceProvider;

    private Set<ReferencedResource> referencedResources = new HashSet<>();

    public TemplateResourcesTool(RenderingMedia renderingMedia, ReferencedResourceProvider referencedResourceProvider) {
        this.renderingMedia = renderingMedia;
        this.referencedResourceProvider = referencedResourceProvider;
    }

    public Set<ReferencedResource> getReferencedResources() {
        return referencedResources;
    }

    protected void configure(ValueParser values) {
        super.configure(values);
    }

    public String load(String path) {
        return this.addResource(path);
    }

    private String addResource(String pathString) {
        Path path = Paths.get(pathString);
        try {
            ReferencedResource referencedResource = this.referencedResourceProvider.findReferencedResourceForPath(path);
            referencedResources.add(referencedResource);
            return createUrl(referencedResource);
        } catch (DispatcherException e) {
            LOG.warn("Failed to create url for resource " + pathString, e);
            return pathString;
        }
    }

    private String createUrl(ReferencedResource referencedResource) {
        String id = referencedResource.getId();
        switch (renderingMedia) {
            case WEB_PAGE:
                try {
                    return this.referencedResourceProvider.getWebUrl(referencedResource)
                            .toString();
                } catch (DispatcherException e) {
                    LOG.warn("Failed to create url for reference resource id " + id, e);
                }
            case NORMAL:
                return "cid:" + id;
        }
        throw new IllegalStateException("rendering option not handled: " + renderingMedia.name());
    }

}

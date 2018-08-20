package com.charlyghislain.dispatcher.api.rendering;


import com.charlyghislain.dispatcher.api.message.ReferencedResource;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.HashSet;
import java.util.Set;

public class RenderedTemplate {

    private InputStream contentStream;
    private Set<ReferencedResource> referencedResources;

    public RenderedTemplate() {
    }

    public RenderedTemplate(InputStream contentStream, Set<ReferencedResource> referencedResources) {
        this.contentStream = contentStream;
        this.referencedResources = referencedResources;
    }

    public static RenderedTemplate compose(RenderedTemplate... templates) {
        InputStream contentStream = null;
        Set<ReferencedResource> referencedResources = new HashSet<>();

        for (RenderedTemplate nextTemplate : templates) {
            Set<ReferencedResource> nextReferencedResources = nextTemplate.getReferencedResources();
            InputStream nextStream = nextTemplate.getContentStream();

            contentStream = contentStream == null ? nextStream : new SequenceInputStream(contentStream, nextStream);
            referencedResources.addAll(nextReferencedResources);
        }

        return new RenderedTemplate(contentStream, referencedResources);
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public RenderedTemplate setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
        return this;
    }

    public Set<ReferencedResource> getReferencedResources() {
        return referencedResources;
    }

    public RenderedTemplate setReferencedResources(Set<ReferencedResource> referencedResources) {
        this.referencedResources = referencedResources;
        return this;
    }
}

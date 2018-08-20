package com.charlyghislain.dispatcher.mail;


import com.charlyghislain.dispatcher.api.exception.DispatcherException;
import com.charlyghislain.dispatcher.api.message.ReferencedResource;

import java.net.URI;
import java.nio.file.Path;

public interface ReferencedResourceProvider {

    ReferencedResource findReferencedResourceForPath(Path path) throws DispatcherException;

    URI getWebUrl(ReferencedResource referencedResource) throws DispatcherException;
}

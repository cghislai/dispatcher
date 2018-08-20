package com.charlyghislain.dispatcher.api.exception;

import java.nio.file.Path;

public class SharedResourceNotFoundException extends DispatcherException {
    private Path path;

    public SharedResourceNotFoundException(Path path) {
        super("Could not find shared resources at " + path);
        this.path = path;
    }
}

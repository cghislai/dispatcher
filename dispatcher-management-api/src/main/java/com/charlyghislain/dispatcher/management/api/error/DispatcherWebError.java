package com.charlyghislain.dispatcher.management.api.error;

public enum DispatcherWebError {
    INVALID_LANGUAGE(400),
    TEMPLATE_RENDERING_ERROR(500),
    TEMPLATE_NOT_FOUND(404),
    RESOURCE_NOT_FOUND(404),
    MESSAGE_NOT_FOUND(404),
    UNEXPECTED_ERROR(500);


    int httpStatus;

    DispatcherWebError(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}

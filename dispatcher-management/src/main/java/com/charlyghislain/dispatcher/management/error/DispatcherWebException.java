package com.charlyghislain.dispatcher.management.error;

import com.charlyghislain.dispatcher.management.api.error.DispatcherWebError;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;

public class DispatcherWebException extends RuntimeException {


    private int status;
    @NotNull
    private String code;
    @Nullable
    private String description;


    public DispatcherWebException(DispatcherWebError webError) {
        this.status = webError.getHttpStatus();
        this.code = webError.name();
    }

    public DispatcherWebException(DispatcherWebError webError, String description) {
        this.status = webError.getHttpStatus();
        this.code = webError.name();
        this.description = description;
    }

    public DispatcherWebException(int status, String code, String description) {
        this.status = status;
        this.code = code;
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

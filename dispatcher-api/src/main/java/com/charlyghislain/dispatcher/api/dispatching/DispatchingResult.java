package com.charlyghislain.dispatcher.api.dispatching;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class DispatchingResult {

    @NotNull
    private RenderingOption renderingOption;
    @NotNull
    private DispatchingOption dispatchingOption;
    private boolean success;
    @Nullable
    private Exception error;
    @Nullable
    private String errorMessage;
    @Nullable
    private LocalDateTime dispatchedTime;
    @Nullable
    private String messageId;

    public RenderingOption getRenderingOption() {
        return renderingOption;
    }

    public void setRenderingOption(RenderingOption renderingOption) {
        this.renderingOption = renderingOption;
    }

    public DispatchingOption getDispatchingOption() {
        return dispatchingOption;
    }

    public void setDispatchingOption(DispatchingOption dispatchingOption) {
        this.dispatchingOption = dispatchingOption;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getDispatchedTime() {
        return dispatchedTime;
    }

    public void setDispatchedTime(LocalDateTime dispatchedTime) {
        this.dispatchedTime = dispatchedTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}

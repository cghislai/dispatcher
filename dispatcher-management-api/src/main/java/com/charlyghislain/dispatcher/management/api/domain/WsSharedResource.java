package com.charlyghislain.dispatcher.management.api.domain;

import javax.validation.constraints.NotNull;

public class WsSharedResource {

    @NotNull
    private String fileName;
    @NotNull
    private String relativePath;
    @NotNull
    private String mimeType;
    // TODO: creation /update date, ...

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}

package com.charlyghislain.dispatcher.api.message;

import java.io.InputStream;

public class MailAttachment {

    private String mimetype;
    private InputStream contentStream;
    private String fileName;

    public String getMimetype() {
        return mimetype;
    }

    public MailAttachment setMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public MailAttachment setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public MailAttachment setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}

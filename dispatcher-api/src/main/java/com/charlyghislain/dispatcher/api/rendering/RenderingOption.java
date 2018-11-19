package com.charlyghislain.dispatcher.api.rendering;


/**
 * Represent a way of rendering a message.
 * Each option has its own template files.
 */
public enum RenderingOption {

    NONE(null),
    LONG_HTML("long-html"),
    LONG_TEXT("long-text"),
    SHORT_TEXT("short-text");

    String templateFileName;

    RenderingOption(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

}

package com.charlyghislain.dispatcher.example.template;

import com.charlyghislain.dispatcher.api.context.TemplateContext;
import com.charlyghislain.dispatcher.api.context.TemplateField;

@TemplateContext(key = "app", produced = true)
public class AppContext {

    @TemplateField(description = "App name", example = "Example Application")
    private String name;

    @TemplateField(description = "App version", example = "1")
    private int version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

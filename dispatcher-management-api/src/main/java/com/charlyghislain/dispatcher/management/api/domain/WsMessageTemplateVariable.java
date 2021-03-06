package com.charlyghislain.dispatcher.management.api.domain;

import com.charlyghislain.dispatcher.management.api.NullableField;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;

public class WsMessageTemplateVariable {

    @NotNull
    private String name;
    @NotNull
    private String description;
    @Nullable
    @NullableField
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

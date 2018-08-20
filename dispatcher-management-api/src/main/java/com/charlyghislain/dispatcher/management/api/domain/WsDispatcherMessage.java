package com.charlyghislain.dispatcher.management.api.domain;

import javax.validation.constraints.NotNull;
import java.util.List;

public class WsDispatcherMessage {

    @NotNull
    private String name;
    @NotNull
    private String qualifiedName;
    @NotNull
    private String description;
    @NotNull
    private List<WsDispatchingOption> dispatchingOptions;
    private boolean compositionItem;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WsDispatchingOption> getDispatchingOptions() {
        return dispatchingOptions;
    }

    public void setDispatchingOptions(List<WsDispatchingOption> dispatchingOptions) {
        this.dispatchingOptions = dispatchingOptions;
    }

    public boolean isCompositionItem() {
        return compositionItem;
    }

    public WsDispatcherMessage setCompositionItem(boolean compositionItem) {
        this.compositionItem = compositionItem;
        return this;
    }
}

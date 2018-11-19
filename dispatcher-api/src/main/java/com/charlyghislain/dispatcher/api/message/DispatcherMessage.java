package com.charlyghislain.dispatcher.api.message;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DispatcherMessage {

    @NotNull
    private Class<?> messageType;
    @NotNull
    private List<Class<?>> templateContexts;
    @Nullable
    private DispatcherMessage header;
    @Nullable
    private DispatcherMessage footer;

    @NotNull
    private String qualifiedName;
    @NotNull
    private String name;
    @NotNull
    private Set<RenderingOption> renderingOptions;
    @NotNull
    private String description;
    @NotNull
    private boolean compositionItem;

    public Class<?> getMessageType() {
        return messageType;
    }

    public void setMessageType(Class<?> messageType) {
        this.messageType = messageType;
    }

    public List<Class<?>> getTemplateContexts() {
        return templateContexts;
    }

    public void setTemplateContexts(List<Class<?>> templateContexts) {
        this.templateContexts = templateContexts;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RenderingOption> getRenderingOptions() {
        return renderingOptions;
    }

    public void setRenderingOptions(Set<RenderingOption> renderingOptions) {
        this.renderingOptions = renderingOptions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Optional<DispatcherMessage> getHeader() {
        return Optional.ofNullable(header);
    }

    public DispatcherMessage setHeader(DispatcherMessage header) {
        this.header = header;
        return this;
    }

    public Optional<DispatcherMessage> getFooter() {
        return Optional.ofNullable(footer);
    }

    public DispatcherMessage setFooter(DispatcherMessage footer) {
        this.footer = footer;
        return this;
    }

    public boolean isCompositionItem() {
        return compositionItem;
    }

    public DispatcherMessage setCompositionItem(boolean compositionItem) {
        this.compositionItem = compositionItem;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DispatcherMessage that = (DispatcherMessage) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "DispatcherMessage{" +
                "name='" + name + '\'' +
                ", renderingOptions=" + renderingOptions +
                ", compositionItem=" + compositionItem +
                '}';
    }
}

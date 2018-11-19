package com.charlyghislain.dispatcher.api.filter;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DispatcherMessageFilter {

    @Nullable
    private RenderingOption renderingOption;
    @Nullable
    private String nameContains;
    @Nullable
    private String name;
    @Nullable
    private String packageContains;

    public RenderingOption getRenderingOption() {
        return renderingOption;
    }

    public void setRenderingOption(RenderingOption renderingOption) {
        this.renderingOption = renderingOption;
    }

    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageContains() {
        return packageContains;
    }

    public void setPackageContains(String packageContains) {
        this.packageContains = packageContains;
    }
}

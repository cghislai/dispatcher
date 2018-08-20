package com.charlyghislain.dispatcher.api.filter;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DispatcherMessageFilter {

    @Nullable
    private DispatchingOption dispatchingOption;
    @Nullable
    private String nameContains;
    @Nullable
    private String name;
    @Nullable
    private String packageContains;

    public DispatchingOption getDispatchingOption() {
        return dispatchingOption;
    }

    public void setDispatchingOption(DispatchingOption dispatchingOption) {
        this.dispatchingOption = dispatchingOption;
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

package com.charlyghislain.dispatcher.management.api.domain;

import com.charlyghislain.dispatcher.management.api.NullableField;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.ws.rs.QueryParam;

public class WsDispatcherMessageFilter {

//    @NullableField
    @Nullable
    @NullableField
    @QueryParam("nameContains")
    private String nameContains;
    //TODO: dispatching type, other tags, ...


    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }
}

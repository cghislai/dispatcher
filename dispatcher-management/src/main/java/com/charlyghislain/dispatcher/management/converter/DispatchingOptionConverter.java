package com.charlyghislain.dispatcher.management.converter;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatchingOption;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DispatchingOptionConverter {

    public RenderingOption toDispatchingOption(WsDispatchingOption wsDispatchingOption) {
        return RenderingOption.valueOf(wsDispatchingOption.name());
    }
}

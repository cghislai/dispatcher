package com.charlyghislain.dispatcher.management.converter;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatchingOption;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DispatchingOptionConverter {

    public DispatchingOption toDispatchingOption(WsDispatchingOption wsDispatchingOption) {
        return DispatchingOption.valueOf(wsDispatchingOption.name());
    }
}

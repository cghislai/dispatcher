package com.charlyghislain.dispatcher.management.converter;

import com.charlyghislain.dispatcher.api.filter.DispatcherMessageFilter;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessageFilter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DispatcherMessageFilterConverter {

   public  DispatcherMessageFilter toDispatcherMessageFilter(WsDispatcherMessageFilter wsDispatcherMessageFilter) {
        String nameContains = wsDispatcherMessageFilter.getNameContains();

        DispatcherMessageFilter dispatcherMessageFilter = new DispatcherMessageFilter();
        dispatcherMessageFilter.setNameContains(nameContains);
        return dispatcherMessageFilter;
    }
}

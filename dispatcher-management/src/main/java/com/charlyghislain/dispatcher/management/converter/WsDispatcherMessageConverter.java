package com.charlyghislain.dispatcher.management.converter;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatcherMessage;
import com.charlyghislain.dispatcher.management.api.domain.WsDispatchingOption;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class WsDispatcherMessageConverter {

    public WsDispatcherMessage toWsDispatcherMessage(DispatcherMessage dispatcherMessage) {
        String qualifiedName = dispatcherMessage.getQualifiedName();
        String name = dispatcherMessage.getName();
        Set<RenderingOption> renderingOptions = dispatcherMessage.getRenderingOptions();
        String description = dispatcherMessage.getDescription();
        boolean compositionItem = dispatcherMessage.isCompositionItem();

        List<WsDispatchingOption> wsDispatchingOptions = renderingOptions.stream()
                .map(Enum::name)
                .map(WsDispatchingOption::valueOf)
                .collect(Collectors.toList());


        WsDispatcherMessage wsDispatcherMessage = new WsDispatcherMessage();
        wsDispatcherMessage.setName(name);
        wsDispatcherMessage.setDescription(description);
        wsDispatcherMessage.setDispatchingOptions(wsDispatchingOptions);
        wsDispatcherMessage.setQualifiedName(qualifiedName);
        wsDispatcherMessage.setCompositionItem(compositionItem);
        return wsDispatcherMessage;
    }
}

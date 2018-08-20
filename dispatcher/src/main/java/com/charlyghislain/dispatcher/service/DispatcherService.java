package com.charlyghislain.dispatcher.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchedMessage;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingResult;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;
import com.charlyghislain.dispatcher.api.service.MessageDispatcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Stateless
public class DispatcherService implements MessageDispatcher {

    @Inject
    private MailDispatcherService mailDispatcherService;

    @Override
    public DispatchedMessage dispatchMessage(RenderedMessage renderedMessage) {

        Set<DispatchingResult> dispatchingResults = new HashSet<>();

        Optional.ofNullable(renderedMessage.getRenderedMailMessage())
                .map(mailDispatcherService::dispatchMessages)
                .ifPresent(dispatchingResults::addAll);


        Map<DispatchingOption, DispatchingResult> dispatchingResultMap = dispatchingResults.stream()
                .collect(Collectors.toMap(
                        DispatchingResult::getDispatchingOption,
                        Function.identity()
                ));

        DispatchedMessage dispatchedMessage = new DispatchedMessage();
        dispatchedMessage.setDispatchingResults(dispatchingResultMap);
        return dispatchedMessage;
    }

}

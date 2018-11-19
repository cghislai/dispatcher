package com.charlyghislain.dispatcher.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchedMessage;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingResult;
import com.charlyghislain.dispatcher.api.rendering.*;
import com.charlyghislain.dispatcher.api.service.MessageDispatcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

@Stateless
public class DispatcherService implements MessageDispatcher {

    @Inject
    private MailDispatcherService mailDispatcherService;

    @Override
    public DispatchedMessage dispatchMessage(RenderedMessage renderedMessage, boolean acceptFirstSuccessOption) {
        List<RenderedMessageDispatchingOption> renderedMessageDispatchingOptions = renderedMessage.getRenderedMessageDispatchingOptions();
        Locale renderedLocale = renderedMessage.getRenderedLocale();

        DispatchedMessage dispatchedMessage = new DispatchedMessage(renderedLocale);

        for (RenderedMessageDispatchingOption dispatchingOption : renderedMessageDispatchingOptions) {
            Set<DispatchingResult> dispatchingResults = dispatchMessageOption(dispatchingOption);
            dispatchedMessage.getDispatchingResultList().addAll(dispatchingResults);
            boolean hasSuccess = dispatchingResults.stream().anyMatch(DispatchingResult::isSuccess);
            if (acceptFirstSuccessOption && hasSuccess) {
                break;
            }
        }

        return dispatchedMessage;
    }

    private Set<DispatchingResult> dispatchMessageOption(RenderedMessageDispatchingOption renderedMessageOption) {
        @NotNull DispatchingOption dispatchingOption = renderedMessageOption.getDispatchingOption();

        switch (dispatchingOption) {
            case MAIL: {
                return dispatchMailMessage((RenderedMailMessage) renderedMessageOption);
            }
            default: {
                DispatchingResult dispatchingResult = new DispatchingResult();
                dispatchingResult.setErrorMessage("Dispatching option not implemented");
                dispatchingResult.setDispatchingOption(dispatchingOption);
                return Collections.singleton(dispatchingResult);
            }
        }
    }

    private Set<DispatchingResult> dispatchMailMessage(RenderedMailMessage renderedMessageOption) {
        return mailDispatcherService.dispatchMessages(renderedMessageOption);
    }


}

package com.charlyghislain.dispatcher.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchedMessage;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingResult;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;
import com.charlyghislain.dispatcher.api.rendering.*;
import com.charlyghislain.dispatcher.api.service.MessageDispatcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.*;

@Stateless
public class DispatcherService implements MessageDispatcher {

    @Inject
    private MailDispatcherService mailDispatcherService;

    @Override
    public DispatchedMessage dispatchMessage(RenderedMessage renderedMessage, boolean breakOnFirstOptionSuccess) {
        List<RenderedMessageDispatchingOption> renderedMessageDispatchingOptions = renderedMessage.getRenderedMessageDispatchingOptions();
        Locale locale = renderedMessage.getRenderedLocale();
        DispatcherMessage message = renderedMessage.getMessage();

        DispatchedMessage dispatchedMessage = new DispatchedMessage(message, locale);

        for (RenderedMessageDispatchingOption dispatchingOption : renderedMessageDispatchingOptions) {
            Set<DispatchingResult> dispatchingResults = dispatchMessageOption(dispatchingOption);
            dispatchedMessage.getDispatchingResultList().addAll(dispatchingResults);
            boolean hasSuccess = dispatchingResults.stream().anyMatch(DispatchingResult::isSuccess);
            if (breakOnFirstOptionSuccess && hasSuccess) {
                break;
            }
        }

        boolean hasFailure = dispatchedMessage.getDispatchingResultList().stream()
                .anyMatch(r -> !r.isSuccess());
        boolean hasSuccess = dispatchedMessage.getDispatchingResultList().stream()
                .anyMatch(DispatchingResult::isSuccess);
        dispatchedMessage.setAllSucceeded(!hasFailure);
        dispatchedMessage.setAnySucceeded(hasSuccess);
        return dispatchedMessage;
    }

    private Set<DispatchingResult> dispatchMessageOption(RenderedMessageDispatchingOption renderedMessageOption) {
        @NotNull DispatchingOption dispatchingOption = renderedMessageOption.getDispatchingOption();

        switch (dispatchingOption) {
            case MAIL: {
                return dispatchMailMessage((RenderedMailMessage) renderedMessageOption);
            }
            default: {
                DispatchingResult dispatchingResult = new DispatchingResult(RenderingOption.NONE, dispatchingOption, false);
                dispatchingResult.setErrorMessage("Dispatching option not implemented");
                return Collections.singleton(dispatchingResult);
            }
        }
    }

    private Set<DispatchingResult> dispatchMailMessage(RenderedMailMessage renderedMessageOption) {
        return mailDispatcherService.dispatchMessages(renderedMessageOption);
    }


}

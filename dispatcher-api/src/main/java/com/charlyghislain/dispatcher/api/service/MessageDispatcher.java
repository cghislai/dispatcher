package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;

public interface MessageDispatcher {

    /**
     * Dispatchs the message and returns an aggregated result.
     *
     * @param renderedMessage              a rendered message.
     * @param acceptFirstDispatchingOption when true, if multiple dispatching options have been rendered, they will be
     *                                     iterated by order of preference until dispatching succeeds. The returned DispatchedMessage
     *                                     may not contain a result for every DispatchingOption provided.
     * @return a DispatchedMessage containing the dispatching outcome.
     */
    DispatchedMessage dispatchMessage(RenderedMessage renderedMessage, boolean acceptFirstDispatchingOption);

    /**
     * Dispatch the message, accepting first success.
     *
     * @param renderedMessage
     * @return
     */
    default DispatchedMessage dispatchMessage(RenderedMessage renderedMessage) {
        return dispatchMessage(renderedMessage, true);
    }
}

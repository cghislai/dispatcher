package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.dispatching.DispatchedMessage;
import com.charlyghislain.dispatcher.api.rendering.RenderedMessage;

public interface MessageDispatcher {

    DispatchedMessage dispatchMessage(RenderedMessage renderedMessage);
}

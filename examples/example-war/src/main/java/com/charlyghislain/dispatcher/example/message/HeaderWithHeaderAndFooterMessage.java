package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;

@MessageDefinition(name = "test/header-with-footer",
        dispatchingOptions = {RenderingOption.LONG_HTML, RenderingOption.LONG_TEXT, RenderingOption.SHORT_TEXT},
        templateContexts = {AppContext.class},
        header = ExampleHeaderMessage.class,
        footer = ExampleFooterMessage.class,
        compositionItem = true)
public class HeaderWithHeaderAndFooterMessage {
}

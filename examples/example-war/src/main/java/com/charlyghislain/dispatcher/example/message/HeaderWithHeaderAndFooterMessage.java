package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;

@MessageDefinition(name = "test/header-with-footer",
        dispatchingOptions = {DispatchingOption.MAIL_HTML, DispatchingOption.MAIL_TEXT, DispatchingOption.SMS},
        templateContexts = {AppContext.class},
        header = ExampleHeaderMessage.class,
        footer = ExampleFooterMessage.class,
        compositionItem = true)
public class HeaderWithHeaderAndFooterMessage {
}

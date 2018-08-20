package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;

@MessageDefinition(name = "header",
        dispatchingOptions = {DispatchingOption.MAIL_HTML, DispatchingOption.MAIL_TEXT, DispatchingOption.SMS},
        templateContexts = {AppContext.class},
        compositionItem = true)
public class ExampleHeaderMessage {
}

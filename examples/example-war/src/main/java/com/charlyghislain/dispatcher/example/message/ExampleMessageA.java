package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.header.MailHeaders;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;
import com.charlyghislain.dispatcher.example.template.RequestContext;

@MessageDefinition(name = "message-a", description = "A first example",
        templateContexts = {AppContext.class, RequestContext.class},
        dispatchingOptions = {DispatchingOption.MAIL_HTML, DispatchingOption.MAIL_TEXT, DispatchingOption.SMS},
        header = ExampleHeaderMessage.class)
@MailHeaders(subject = "An example subject")
public class ExampleMessageA {
}

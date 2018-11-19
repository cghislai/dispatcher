package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.header.MailHeaders;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;
import com.charlyghislain.dispatcher.example.template.RequestContext;

@MessageDefinition(name = "message-a", description = "A first example",
        templateContexts = {AppContext.class, RequestContext.class},
        dispatchingOptions = {RenderingOption.LONG_HTML, RenderingOption.LONG_TEXT, RenderingOption.SHORT_TEXT},
        header = ExampleHeaderMessage.class)
@MailHeaders(subject = "An example subject")
public class ExampleMessageA {
}

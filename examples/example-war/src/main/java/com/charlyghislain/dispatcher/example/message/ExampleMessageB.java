package com.charlyghislain.dispatcher.example.message;

import com.charlyghislain.dispatcher.api.rendering.RenderingOption;
import com.charlyghislain.dispatcher.api.header.MailHeaders;
import com.charlyghislain.dispatcher.api.message.MessageDefinition;
import com.charlyghislain.dispatcher.example.template.AppContext;
import com.charlyghislain.dispatcher.example.template.RequestContext;

@MessageDefinition(name = "test/message-b", description = "Another example",
        templateContexts = {AppContext.class, RequestContext.class},
        dispatchingOptions = {RenderingOption.LONG_HTML},
        header = HeaderWithHeaderAndFooterMessage.class,
        footer = ExampleFooterMessage.class
)
@MailHeaders(subject = "Default subject in code",
        to = "${request.userName}@users.example.com; Michaël Porti <michael-porti@example.com>")
public class ExampleMessageB {
}

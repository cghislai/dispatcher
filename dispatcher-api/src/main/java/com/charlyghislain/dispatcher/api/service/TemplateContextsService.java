package com.charlyghislain.dispatcher.api.service;


import com.charlyghislain.dispatcher.api.context.TemplateContextObject;
import com.charlyghislain.dispatcher.api.context.TemplateVariableDescription;
import com.charlyghislain.dispatcher.api.message.DispatcherMessage;

import java.util.List;

public interface TemplateContextsService {

    List<TemplateContextObject> createTemplateContexts(DispatcherMessage message, Object... contextObjects);

    List<TemplateContextObject> createExampleTemplateContexts(DispatcherMessage message);

    List<TemplateVariableDescription> listTemplateVariableDescriptions(DispatcherMessage message);

}

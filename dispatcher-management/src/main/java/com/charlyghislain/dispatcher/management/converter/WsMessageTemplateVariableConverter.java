package com.charlyghislain.dispatcher.management.converter;


import com.charlyghislain.dispatcher.api.context.TemplateVariableDescription;
import com.charlyghislain.dispatcher.management.api.domain.WsMessageTemplateVariable;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WsMessageTemplateVariableConverter {

    public WsMessageTemplateVariable toWsMessageTemplateVariable(TemplateVariableDescription variableDescription) {
        String variableName = variableDescription.getVariableName();
        String description = variableDescription.getDescription();
        String example = variableDescription.getExample();

        WsMessageTemplateVariable templateVariable = new WsMessageTemplateVariable();
        templateVariable.setName(variableName);
        templateVariable.setDescription(description);
        templateVariable.setValue(example);
        return templateVariable;
    }
}

package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;
import com.charlyghislain.dispatcher.api.message.MailAttachment;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderedMailMessage implements RenderedMessageDispatchingOption {

    @NotNull
    private RenderedMailHeaders renderedHeaders;
    @NotNull
    private List<MailAttachment> mailAttachments = new ArrayList<>();
    @NotNull
    private Map<RenderingOption, RenderedTemplate> renderedTemplates;


    @Override
    public DispatchingOption getDispatchingOption() {
        return DispatchingOption.MAIL;
    }


    @Override
    public RenderedMailHeaders getRenderedHeaders() {
        return renderedHeaders;
    }

    public void setRenderedHeaders(RenderedMailHeaders renderedHeaders) {
        this.renderedHeaders = renderedHeaders;
    }

    public List<MailAttachment> getMailAttachments() {
        return mailAttachments;
    }

    public void setMailAttachments(List<MailAttachment> mailAttachments) {
        this.mailAttachments = mailAttachments;
    }

    @Override
    public Map<RenderingOption, RenderedTemplate> getRenderedTemplates() {
        return renderedTemplates;
    }

    public void setRenderedTemplates(Map<RenderingOption, RenderedTemplate> renderedTemplates) {
        this.renderedTemplates = renderedTemplates;
    }
}

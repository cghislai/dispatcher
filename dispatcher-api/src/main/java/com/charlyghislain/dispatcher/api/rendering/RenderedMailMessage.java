package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.message.MailAttachment;
import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderedMailMessage {

    @NotNull
    private RenderedMailHeaders mailMessageHeaders;
    @NotNull
    @Size(min = 1)
    private Map<DispatchingOption, RenderedTemplate> renderedTemplates;
    @NotNull
    private List<MailAttachment> mailAttachments = new ArrayList<>();

    public RenderedMailHeaders getMailMessageHeaders() {
        return mailMessageHeaders;
    }

    public RenderedMailMessage setMailMessageHeaders(RenderedMailHeaders mailMessageHeaders) {
        this.mailMessageHeaders = mailMessageHeaders;
        return this;
    }

    public Map<DispatchingOption, RenderedTemplate> getRenderedTemplates() {
        return renderedTemplates;
    }

    public void setRenderedTemplates(Map<DispatchingOption, RenderedTemplate> renderedTemplates) {
        this.renderedTemplates = renderedTemplates;
    }

    public List<MailAttachment> getMailAttachments() {
        return mailAttachments;
    }

    public RenderedMailMessage setMailAttachments(List<MailAttachment> mailAttachments) {
        this.mailAttachments = mailAttachments;
        return this;
    }
}

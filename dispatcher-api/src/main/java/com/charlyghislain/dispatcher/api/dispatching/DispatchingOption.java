package com.charlyghislain.dispatcher.api.dispatching;

import com.charlyghislain.dispatcher.api.header.MailHeadersTemplate;
import com.charlyghislain.dispatcher.api.header.MessageHeaders;
import com.charlyghislain.dispatcher.api.header.PhoneHeadersTemplate;
import com.charlyghislain.dispatcher.api.rendering.DispatchingRenderingOption;
import com.charlyghislain.dispatcher.api.rendering.RenderingOption;

import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

/**
 * Represent a way to transmit the message to the end user. A single option may contain
 * multiple rendered messages for different {@link RenderingOption}.
 * <p>
 * For instance, a mail could contain an html message with a fallback for the text message, and a phone communication
 * could contain a push notification message with a fallback for an sms.
 */
public enum DispatchingOption {
    MAIL(MailHeadersTemplate.class),
    PHONE(PhoneHeadersTemplate.class);

    private Class<? extends MessageHeaders> headersType;

    DispatchingOption(Class<? extends MessageHeaders> headersType) {
        this.headersType = headersType;
    }

    public Class<? extends MessageHeaders> getHeadersType() {
        return headersType;
    }

    /**
     * Create a DispatchingRenderingOption requiring all RenderingOption to successfully render.
     *
     * @param renderingOptions Rendering options to consider.
     */
    public DispatchingRenderingOption allRenderingOptions(@Size(min = 1) RenderingOption... renderingOptions) {
        List<RenderingOption> renderingOptionList = Arrays.asList(renderingOptions);

        DispatchingRenderingOption dispatchingRenderingOption = new DispatchingRenderingOption(this, false);
        dispatchingRenderingOption.getRenderingOptionsByOrderOfPreference()
                .addAll(renderingOptionList);
        return dispatchingRenderingOption;
    }

    /**
     * Create a DispatchingRenderingOption requiring any RenderingOption to successfully render.
     *
     * @param renderingOptions Rendering options to consider.
     */
    public DispatchingRenderingOption anyRenderingOption(@Size(min = 1) RenderingOption... renderingOptions) {
        List<RenderingOption> renderingOptionList = Arrays.asList(renderingOptions);

        DispatchingRenderingOption dispatchingRenderingOption = new DispatchingRenderingOption(this, true);
        dispatchingRenderingOption.getRenderingOptionsByOrderOfPreference()
                .addAll(renderingOptionList);
        return dispatchingRenderingOption;
    }

}

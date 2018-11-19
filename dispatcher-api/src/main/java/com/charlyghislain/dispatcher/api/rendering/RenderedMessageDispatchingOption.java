package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

public interface RenderedMessageDispatchingOption {

    @NotNull
    DispatchingOption getDispatchingOption();

    @NotNull
    @Size(min = 1)
    Map<RenderingOption, RenderedTemplate> getRenderedTemplates();

    @NotNull
    RenderedHeaders<?> getRenderedHeaders();


}

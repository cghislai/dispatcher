package com.charlyghislain.dispatcher.api.rendering;

import com.charlyghislain.dispatcher.api.dispatching.DispatchingOption;

import java.util.ArrayList;
import java.util.List;

/**
 * A preference list of rendering options for a single dispatching option.
 *
 * @param <T> the dispatching option.
 * @see DispatchingOption#allRenderingOptions(RenderingOption...)
 * @see DispatchingOption#anyRenderingOption(RenderingOption...)
 */
public class DispatchingRenderingOption {

    private final DispatchingOption dispatchingOption;
    private final List<RenderingOption> renderingOptionsByOrderOfPreference = new ArrayList<>();
    private final boolean acceptAny;

    /**
     * Create an empty DispatchingRenderingOption.
     *
     * @param dispatchingOption dispatching option
     * @param acceptAny         if true, message rendering will succeed if at least one rendering option succeeds.
     *                          Rendering of each option will still be attempted, but optional failures will be silenced.
     */
    public DispatchingRenderingOption(DispatchingOption dispatchingOption, boolean acceptAny) {
        this.dispatchingOption = dispatchingOption;
        this.acceptAny = acceptAny;
    }

    public List<RenderingOption> getRenderingOptionsByOrderOfPreference() {
        return renderingOptionsByOrderOfPreference;
    }

    public DispatchingOption getDispatchingOption() {
        return dispatchingOption;
    }

    public boolean isAcceptAny() {
        return acceptAny;
    }
}

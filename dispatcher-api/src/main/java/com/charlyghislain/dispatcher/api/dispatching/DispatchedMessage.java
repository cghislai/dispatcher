package com.charlyghislain.dispatcher.api.dispatching;

import java.util.Map;

public class DispatchedMessage {

    private Map<DispatchingOption, DispatchingResult> dispatchingResults;

    public Map<DispatchingOption, DispatchingResult> getDispatchingResults() {
        return dispatchingResults;
    }

    public void setDispatchingResults(Map<DispatchingOption, DispatchingResult> dispatchingResults) {
        this.dispatchingResults = dispatchingResults;
    }
}

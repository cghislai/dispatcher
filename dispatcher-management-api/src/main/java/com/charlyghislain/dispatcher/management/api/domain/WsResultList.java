package com.charlyghislain.dispatcher.management.api.domain;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class WsResultList<T extends Object> {

    @NotNull
    private List<T> results;
    private long totalCount;

    public WsResultList() {
        this.results = new ArrayList<>();
        this.totalCount = 0L;
    }

    public WsResultList(@NotNull List<T> results, long totalCount) {
        this.results = results;
        this.totalCount = totalCount;
    }

    public WsResultList(@NotNull List<T> results, int totalCount) {
        this.results = results;
        this.totalCount = (long) totalCount;
    }

    @NotNull
    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}

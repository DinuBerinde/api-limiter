package com.dinuberinde.api.limiter.internal;

import net.jcip.annotations.Immutable;

/**
 * Class to keep track of the number of calls and time made by a clinet for an API.
 */
@Immutable
class ApiCall {
    private final int numberOfCalls;
    private final long time;
    private final String client;
    private final String api;

    ApiCall(int numberOfCalls, long time, String client, String api) {
        this.numberOfCalls = numberOfCalls;
        this.time = time;
        this.client = client;
        this.api = api;
    }

    public long getTime() {
        return time;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    public String getClient() {
        return client;
    }

    public String getApi() {
        return api;
    }
}

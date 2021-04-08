package com.dinuberinde.api.limiter;

import net.jcip.annotations.Immutable;

import java.util.Arrays;

/**
 * Class to configure the maximum number of calls that a client can consume within a certain timeframe for an API.
 * The configuration can be applied to a specific client or to all clients.
 */
@Immutable
public final class ApiConfig {
    private final static int DEFAULT_MAX_CALLS = 5;
    private final static long DEFAULT_TIMEFRAME = 10 * 1000;
    /**
     * A token that represents all clients.
     */
    public final static String ALL_CLIENTS = "*";

    private final String apiName;
    private final String client;
    private final int maxCalls;
    private final long timeframe;


    /**
     * Configuration of the API to make maxCalls calls in a time interval
     * of interval seconds on behalf of a client.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given timeframe
     * @param timeframe the timeframe in which a client can consume API calls, in seconds
     * @param client the client name or * if intended for all clients
     */
    public ApiConfig(String apiName, int maxCalls, long timeframe, String client) {
        this.apiName = apiName;
        this.maxCalls = maxCalls;
        this.timeframe = timeframe;
        this.client = client;
    }

    /**
     * Configuration of the API to make maxCalls calls in a time interval
     * of interval seconds. The API configuration applies to ALL clients.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given timeframe
     * @param timeframe the timeframe in which a client can consume API calls, in seconds.
     */
    public ApiConfig(String apiName, int maxCalls, long timeframe) {
        this(apiName, maxCalls, timeframe, ALL_CLIENTS);
    }

    /**
     * Configuration of the Api.
     * The default configuration allows 5 calls to all clients in a timeframe of 10 seconds.
     * @param apiName the api name
     */
    public ApiConfig(String apiName) {
        this(apiName, DEFAULT_MAX_CALLS, DEFAULT_TIMEFRAME);
    }

    /**
     * Configuration of the Api.
     * The default configuration allows 5 calls to a client in a timeframe of 10 seconds.
     * @param apiName the api name
     * @param client the token
     */
    public ApiConfig(String apiName, String client) {
        this(apiName, DEFAULT_MAX_CALLS, DEFAULT_TIMEFRAME, client);
    }

    public String getApiName() {
        return apiName;
    }

    public int getMaxCalls() {
        return maxCalls;
    }

    public long getTimeFrame() {
        return timeframe;
    }

    public String getClient() {
        return client;
    }

    /**
     * Helper method to build an array of {@link ApiConfig} for the given clients.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given timeframe
     * @param timeframe the timeframe in which a client can consume API calls, in seconds
     * @param clients the clients
     * @return an array of {@link ApiConfig}
     */
    public static ApiConfig[] of(String apiName, int maxCalls, long timeframe, String... clients) {
        if (clients == null) {
            throw new ApiLimiterException("Clients cannot be null");
        }

        return Arrays.stream(clients)
                .map(client -> new ApiConfig(apiName, maxCalls, timeframe, client))
                .toArray(ApiConfig[]::new);
    }
}

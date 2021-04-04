package api.limiter.internal;

import net.jcip.annotations.Immutable;

/**
 * Class to configure the maximum number of calls that a client can make within a certain timeframe for an API.
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
     * @param maxCalls the max calls allowed in a given interval of time
     * @param timeframe the timeframe in which a client can make API calls, in seconds
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
     * @param maxCalls the max calls allowed in a given interval of time
     * @param timeframe the timeframe in which a client can make API calls, in seconds.
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
}

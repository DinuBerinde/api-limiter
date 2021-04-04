package api.limiter.internal;

import net.jcip.annotations.Immutable;

/**
 * Class to configure the maximum number of calls in a time interval for an API on behalf of a client.
 * The configuration can be applied to a specific client or to all clients.
 */
@Immutable
public final class ApiConfig {
    private final static int MAX_CALLS_DEFAULT = 5;
    private final static long INTERVAL_DEFAULT = 10 * 1000;
    /**
     * A token that represents all clients.
     */
    public final static String ALL_CLIENTS = "*";

    private final String apiName;
    private final String client;
    private final int maxCalls;
    private final long interval;


    /**
     * Configuration of the API to make maxCalls calls in a time interval
     * of interval seconds on behalf of a client.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given interval of time
     * @param interval the time interval to make the calls, in seconds
     * @param client the client name or * if intended for all clients
     */
    public ApiConfig(String apiName, int maxCalls, long interval, String client) {
        this.apiName = apiName;
        this.maxCalls = maxCalls;
        this.interval = interval;
        this.client = client;
    }

    /**
     * Configuration of the API to make maxCalls calls in a time interval
     * of interval seconds. The API configuration applies to ALL clients.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given interval of time
     * @param interval the time interval to make the calls, in seconds.
     */
    public ApiConfig(String apiName, int maxCalls, long interval) {
        this(apiName, maxCalls, interval, ALL_CLIENTS);
    }

    /**
     * Configuration of the Api.
     * The default configuration allows 5 calls in a time interval of 10 seconds to all clients.
     * @param apiName the api name
     */
    public ApiConfig(String apiName) {
        this(apiName, MAX_CALLS_DEFAULT, INTERVAL_DEFAULT);
    }

    /**
     * Configuration of the Api.
     * The default configuration allows 5 calls in a time interval of 10 seconds to a client.
     * @param apiName the api name
     * @param client the token
     */
    public ApiConfig(String apiName, String client) {
        this(apiName, MAX_CALLS_DEFAULT, INTERVAL_DEFAULT, client);
    }

    public String getApiName() {
        return apiName;
    }

    public int getMaxCalls() {
        return maxCalls;
    }

    public long getInterval() {
        return interval;
    }

    public String getClient() {
        return client;
    }
}

package api.limiter.internal;

import net.jcip.annotations.Immutable;

/**
 * Class to configure the maximum number of calls in a time interval for an API.
 */
@Immutable
public final class ApiConfig {
    private final static int MAX_CALLS_DEFAULT = 5;
    private final static long INTERVAL_DEFAULT = 10 * 1000;

    private final String apiName;
    private final int maxCalls;
    private final long interval;


    /**
     * Configuration of the Api to make maxCalls calls in an interval
     * of interval seconds.
     * @param apiName the api name
     * @param maxCalls the max calls allowed in a given interval of time
     * @param interval the interval of time to make the calls, in seconds.
     */
    public ApiConfig(String apiName, int maxCalls, long interval) {
        this.apiName = apiName;
        this.maxCalls = maxCalls;
        this.interval = interval;
    }

    /**
     * Configuration of the Api.
     * The default configuration allows 5 calls in an interval of 10 seconds.
     * @param apiName the api name
     */
    public ApiConfig(String apiName) {
        this(apiName, MAX_CALLS_DEFAULT, INTERVAL_DEFAULT);
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
}

package api.limiter.internal;

import net.jcip.annotations.ThreadSafe;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe class to limit API calls that a client can make within a certain timeframe.
 */
@ThreadSafe
public final class Limiter {
    private final Map<String, ApiCall> clients = new HashMap<>();
    private final ApiConfig apiConfig;


    public Limiter(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * Consumes an API call on behalf of a client.
     * @param client the client
     * @return true if consumed successfully, false if the current API call exceeds
     * the configured API maximum calls within the configured API timeframe
     */
    public boolean consume(String client) {

        synchronized (this) {

            if (this.clients.containsKey(client)) {
                ApiCall apiCall = this.clients.get(client);

                if (timeframeExpired(apiCall)) {
                    this.clients.put(client, new ApiCall(1, System.currentTimeMillis(), client, apiConfig.getApiName()));
                } else if (callLimitExceeded(apiCall)) {
                    return false;
                } else {
                    this.clients.put(client, new ApiCall(apiCall.getNumberOfCalls() + 1, apiCall.getTime(), apiCall.getClient(), apiCall.getApi()));
                }

            } else {
                this.clients.put(client, new ApiCall(1, System.currentTimeMillis(), client, apiConfig.getApiName()));
            }
        }

        return true;
    }

    /**
     * It checks whether the current API call exceeded the number of maximum calls of the configured API.
     * @param apiCall the api call
     * @return true if the current API call exceeded the number of maximum calls, false otherwise
     */
    private boolean callLimitExceeded(ApiCall apiCall) {
        return apiCall.getNumberOfCalls() + 1 > apiConfig.getMaxCalls();
    }

    /**
     * It checks whether the timeframe of the current API call is not expired, hence it checks whether
     * the timeframe of the call is eligible with respect to the configured timeframe of the API.
     * @param apiCall the api call
     * @return true if the timeframe expired, false otherwise
     */
    private boolean timeframeExpired(ApiCall apiCall) {
        return System.currentTimeMillis() - apiCall.getTime() > apiConfig.getTimeFrame();
    }
}

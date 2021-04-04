package api.limiter.internal;

import net.jcip.annotations.ThreadSafe;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe class to limit API calls made by clients.
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
     * the configured API maximum calls in the configured API time interval
     */
    public boolean consume(String client) {

        synchronized (this) {

            if (this.clients.containsKey(client)) {
                ApiCall apiCall = this.clients.get(client);

                if (intervalExpired(apiCall)) {
                    this.clients.put(client, new ApiCall(1, System.currentTimeMillis()));
                } else if (callLimitExceeded(apiCall)) {
                    return false;
                } else {
                    this.clients.put(client, new ApiCall(apiCall.getNumberOfCalls() + 1, apiCall.getTime()));
                }

            } else {
                this.clients.put(client, new ApiCall(1, System.currentTimeMillis()));
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
     * It checks whether the current API call is not expired, hence it checks whether
     * the time of the call is eligible with respect to the configured time interval of the API.
     * @param apiCall the api call
     * @return true if the interval expired, false otherwise
     */
    private boolean intervalExpired(ApiCall apiCall) {
        return System.currentTimeMillis() - apiCall.getTime() > apiConfig.getInterval();
    }
}

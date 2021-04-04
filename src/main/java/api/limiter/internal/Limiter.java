package api.limiter.internal;

import net.jcip.annotations.ThreadSafe;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe class limit API calls.
 */
@ThreadSafe
public final class Limiter {
    private final Map<String, ApiCall> limits = new HashMap<>();
    private final ApiConfig apiConfig;

    public Limiter(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * Consumes an API call on behalf of the specified token.
     * @param token the token
     * @return true if consumed successfully, false if the number of the current API call exceeds
     * the configured API maximum calls in the configured API interval
     */
    public boolean consume(String token) {

        synchronized (this) {

            if (this.limits.containsKey(token)) {
                ApiCall apiCall = this.limits.get(token);

                if (intervalExpired(apiCall)) {
                    this.limits.put(token, new ApiCall(1, System.currentTimeMillis()));
                } else if (callLimitExceeded(apiCall)) {
                    return false;
                } else {
                    this.limits.put(token, new ApiCall(apiCall.getNumberOfCalls() + 1, apiCall.getTime()));
                }

            } else {
                this.limits.put(token, new ApiCall(1, System.currentTimeMillis()));
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

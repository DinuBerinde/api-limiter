package api.limiter.internal;

import java.util.HashMap;
import java.util.Map;

public final class Limiter {
    private final Map<String, ApiCall> limits = new HashMap<>();
    private final ApiConfig apiConfig;

    public Limiter(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     * Consume an api call on behalf of the specified token.
     * @param token the token
     * @return true if consumed successfully, false if the number of api calls exceeded the configured api interval
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
     * It checks whether the api call exceeded the number of calls of the configured api.
     * @param apiCall the api call
     * @return true if the number of calls exceeded, false otherwise
     */
    private boolean callLimitExceeded(ApiCall apiCall) {
        return apiCall.getNumberOfCalls() + 1 > apiConfig.getMaxCalls();
    }

    /**
     * It checks whether the current api call is not expired.
     * It checks whether the time of the call is eligible with respect to the configured interval of time of the api.
     * @param apiCall the api call
     * @return true if the interval expired, false otherwise
     */
    private boolean intervalExpired(ApiCall apiCall) {
        return System.currentTimeMillis() - apiCall.getTime() > apiConfig.getInterval();
    }
}

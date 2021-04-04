package api.limiter;

import api.limiter.internal.ApiConfig;
import api.limiter.internal.Limiter;
import net.jcip.annotations.ThreadSafe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe class to consume and limit API calls on behalf of a client without
 * exceeding the API's maximum number of calls in a given time interval.
 */
@ThreadSafe
public final class ApiLimiter {
    private final static ApiLimiter INSTANCE = new ApiLimiter();
    private final Map<String, Limiter> limiters = new HashMap<>();

    private ApiLimiter() {}

    /**
     * It registers the APIs to limit.
     * @param apis the apis
     */
    public static void registerApis(ApiConfig... apis) {
        Arrays.stream(apis).forEach(api -> INSTANCE.limiters.put(api.getApiName(), new Limiter(api)));
    }

    /**
     * Consumes an API on behalf of the specified token.
     * @param apiName the api name
     * @param token the token
     * @return true if consumed successfully, false if the number of the current API call exceeds
     * the configured API maximum calls in the configured API interval
     */
    public static boolean consume(String apiName, String token) {

        if (apiName == null) {
            throw new RuntimeException("Api name cannot be null");
        }

        if (token == null) {
            throw new RuntimeException("Token cannot be null");
        }

        Limiter limiter;
        synchronized (INSTANCE) {
            if (INSTANCE.limiters.containsKey(apiName)) {
                limiter = INSTANCE.limiters.get(apiName);
            } else {
                throw new RuntimeException(String.format("Api name %s not found", apiName));
            }
        }

        return limiter.consume(token);
    }
}

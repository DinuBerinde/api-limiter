package com.dinuberinde.api.limiter;

import com.dinuberinde.api.limiter.internal.Limiter;
import net.jcip.annotations.ThreadSafe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe class to consume API calls on behalf of a client or clients and
 * it limits the API calls that a client can consume within a certain timeframe.
 */
@ThreadSafe
public final class ApiLimiter {
    private final static ApiLimiter INSTANCE = new ApiLimiter();
    private final Map<String, Map<String, Limiter>> apiLimiterMap = new HashMap<>();

    private ApiLimiter() {}

    /**
     * It registers the APIs to limit.
     * @param apis the apis
     */
    public static void registerApis(ApiConfig... apis) {
        synchronized (INSTANCE) {
            Arrays.stream(apis).forEach(api -> INSTANCE.apiLimiterMap.computeIfAbsent(api.getApiName(), k -> new HashMap<>()).put(api.getClient(), new Limiter(api)));
        }
    }

    /**
     * Consumes an API on behalf of all clients.
     * @param apiName the api name
     * @return true if consumed successfully, false if the current API call exceeds
     * the configured API maximum calls in the configured API time interval
     */
    public static boolean consume(String apiName) {
        return consume(apiName, ApiConfig.ALL_CLIENTS);
    }

    /**
     * Consumes an API on behalf of a specific client.
     * @param apiName the api name
     * @param client the client name. Ignored if the API was configured for all clients
     * @return true if consumed successfully, false if the current API call exceeds
     * the configured API maximum calls within the configured API timeframe
     */
    public static boolean consume(String apiName, String client) {

        if (apiName == null) {
            throw new ApiLimiterException("API name cannot be null");
        }

        Limiter limiter;
        synchronized (INSTANCE) {
            if (INSTANCE.apiLimiterMap.containsKey(apiName)) {
                Map<String, Limiter> clientLimiterMap = INSTANCE.apiLimiterMap.get(apiName);

                if (clientLimiterMap.containsKey(ApiConfig.ALL_CLIENTS)) {
                    limiter = clientLimiterMap.get(ApiConfig.ALL_CLIENTS);
                } else if (client == null) {
                    throw new ApiLimiterException("Client cannot be null");
                } else if (clientLimiterMap.containsKey(client)) {
                    limiter = clientLimiterMap.get(client);
                } else {
                    throw new ApiLimiterException(String.format("Client %s non found for API %s", client, apiName));
                }

            } else {
                throw new ApiLimiterException(String.format("API %s not registered", apiName));
            }
        }

        return limiter.consume(client);
    }
}

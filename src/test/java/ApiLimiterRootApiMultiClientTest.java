import com.dinuberinde.api.limiter.ApiConfig;
import com.dinuberinde.api.limiter.ApiLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiLimiterRootApiMultiClientTest {
    private final static String ROOT_API = "/root-api-client/*";


    @Test
    @DisplayName("MaxCalls = 10, Timeframe = 5sec -> Should allow at most 10 calls per root path per client")
    public void shouldAllowAtMost10CallsPerRootPathPerClient() {
        ApiLimiter.registerApis(
                new ApiConfig(ROOT_API, 10, 5 * 1000, "client-1"),
                new ApiConfig(ROOT_API, 10, 5 * 1000, "client-2")
        );

        List<Boolean> consumers = new ArrayList<>(20);
        for (int i = 1; i < 11; i++) {
            consumers.add(ApiLimiter.consume("/root-api-client/endpoint/" + i, "client-1"));
            consumers.add(ApiLimiter.consume("/root-api-client/endpoint/" + i, "client-2"));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume("/root-api-client/endpoint/1", "client-1")); // should not consume the 11th call
        Assertions.assertFalse(ApiLimiter.consume("/root-api-client/endpoint/1", "client-2")); // should not consume the 11th call
    }

    @Test
    @DisplayName("MaxCalls = 10, Timeframe = 5sec -> Should allow at most 10 calls per root path per client with ApiConfig.of() configuration")
    public void shouldAllowAtMost10CallsPerRootPathPerClientOfConfig() {
        ApiLimiter.registerApis(
                ApiConfig.of(ROOT_API, 10, 5 * 1000, "client-1", "client-2")
        );

        List<Boolean> consumers = new ArrayList<>(20);
        for (int i = 1; i < 11; i++) {
            consumers.add(ApiLimiter.consume("/root-api-client/endpoint/" + i, "client-1"));
            consumers.add(ApiLimiter.consume("/root-api-client/endpoint/" + i, "client-2"));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume("/root-api-client/endpoint/1", "client-1")); // should not consume the 11th call
        Assertions.assertFalse(ApiLimiter.consume("/root-api-client/endpoint/1", "client-2")); // should not consume the 11th call
    }

}

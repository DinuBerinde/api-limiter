import com.dinuberinde.api.limiter.ApiConfig;
import com.dinuberinde.api.limiter.ApiLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiLimiterRootApiAllClientsTest {
    private final static String ROOT_API = "/root-api/*";


    @Test
    @DisplayName("MaxCalls = 10, Timeframe = 5sec -> Should allow at most 10 calls for root path for all clients")
    public void shouldAllowAtMost10CallsPerRootPath() {
        ApiLimiter.registerApis(new ApiConfig(ROOT_API, 10, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i < 11; i++) {
            consumers.add(ApiLimiter.consume("/root-api/endpoint/" + i));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume("/root-api/endpoint/1")); // should not consume the 11th call
    }

    @Test
    @DisplayName("MaxCalls = 10, Timeframe = 5sec -> Should allow at most 10 calls per root path for all clients")
    public void shouldAllowAtMost10CallsPerRootPathMultiple() {
        ApiLimiter.registerApis(
                new ApiConfig(ROOT_API, 10, 5 * 1000),
                new ApiConfig("/root-api-second/*", 10, 5 * 1000)
        );

        List<Boolean> consumers = new ArrayList<>(20);
        for (int i = 1; i < 11; i++) {
            consumers.add(ApiLimiter.consume("/root-api/endpoint/" + i));
            consumers.add(ApiLimiter.consume("/root-api-second/endpoint/" + i));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume("/root-api/endpoint/1")); // should not consume the 11th call
        Assertions.assertFalse(ApiLimiter.consume("/root-api-second/endpoint/1")); // should not consume the 11th call
    }
}

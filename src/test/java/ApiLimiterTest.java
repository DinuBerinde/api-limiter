import api.limiter.ApiLimiter;
import api.limiter.internal.ApiConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiLimiterTest {
    private final static String API_NAME = "/api/test";
    private final static String TOKEN = "alkmncbvxerop";

    @Test
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should allow 5 calls to api")
    public void shouldAllow5Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 5; i++) {
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should limit 6 calls to api")
    public void shouldLimit6Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, TOKEN));
    }

    @Test
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should allow 8 calls on api")
    public void shouldAllow8Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should limit 10 calls")
    public void shouldFailOn10Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, TOKEN));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, TOKEN));
    }

    @Test
    @DisplayName("MaxCalls = 5, Interval = 3sec -> Should allow 10 calls: 5 calls + interval reset + 5 calls")
    public void shouldAllow10CallsWithIntervalReset() throws InterruptedException {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the interval reset
            }
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Interval = 3sec -> Should limit consuming 12 calls: 5 calls + interval reset + 5 calls + 2 calls")
    public void shouldLimit12CallsWithIntervalReset() throws InterruptedException {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the interval reset
            }
            consumers.add(ApiLimiter.consume(API_NAME, TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, TOKEN));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, TOKEN));
    }
}

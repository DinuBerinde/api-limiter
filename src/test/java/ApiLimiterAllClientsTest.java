import com.dinuberinde.api.limiter.ApiLimiter;
import com.dinuberinde.api.limiter.ApiLimiterException;
import com.dinuberinde.api.limiter.ApiConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiLimiterAllClientsTest {
    private final static String API_NAME = "/api/test";


    @Test
    @DisplayName("Should launch an ApiLimiterException when consuming a null API")
    public void shouldFailOnNullApiName() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        try {
            ApiLimiter.consume(null);
        } catch (ApiLimiterException e) {
            Assertions.assertEquals("API name cannot be null", e.getMessage());
            return;
        }

        Assertions.fail();
    }


    @Test
    @DisplayName("Should launch an ApiLimiterException when consuming an unregistered API")
    public void shouldFailOnUnregisteredAPI() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        try {
            ApiLimiter.consume("/api/failingApi");
        } catch (ApiLimiterException e) {
            Assertions.assertEquals("API /api/failingApi not registered", e.getMessage());
            return;
        }

        Assertions.fail();
    }


    @Test
    @DisplayName("Should allow consuming API call on behalf of a specific client")
    public void shouldAllowCallOnBehalfASpecificClient() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));
        Assertions.assertTrue(ApiLimiter.consume(API_NAME));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow 5 calls to an API for 3 clients")
    public void shouldAllow5CallsSameApiMultipleClients() {
        ApiLimiter.registerApis(ApiConfig.of(
                API_NAME,
                5,
                10 * 1000,
                "c1",
                "c2",
                "c3"
                )
        );

        List<Boolean> consumers = new ArrayList<>(15);
        for (int c = 1; c < 4; c++) {
            for (int i = 1; i <= 5; i++) {
                consumers.add(ApiLimiter.consume(API_NAME, "c" + c));
            }
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        // should fail consuming api
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, "c1"));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow 5 calls to API")
    public void shouldAllow5Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 5; i++) {
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit 1 call out of 6 calls to API")
    public void shouldFailOn6Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME));
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should allow 8 calls on API")
    public void shouldAllow8Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should limit 2 calls out of 10 calls")
    public void shouldFailOn10Calls() {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 3sec -> Should allow 10 calls: 5 calls + timeframe reset + 5 calls")
    public void shouldAllow10CallsWithTimeframeReset() throws InterruptedException {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the timeframe reset
            }
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 3sec -> Should fail consuming 2 calls out of 12 calls: 5 calls + timeframe reset + 5 calls + 2 calls")
    public void shouldFailOn12CallsWithTimeframeReset() throws InterruptedException {
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the timeframe reset
            }
            consumers.add(ApiLimiter.consume(API_NAME));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME));
    }
}

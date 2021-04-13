import com.dinuberinde.api.limiter.ApiConfig;
import com.dinuberinde.api.limiter.ApiLimiter;
import com.dinuberinde.api.limiter.ApiLimiterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApiLimiterMultiClientTest {
    private final static String API_NAME = "/api/multiple-clients-test";

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

        try {
            Assertions.assertFalse(ApiLimiter.consume(API_NAME));
        } catch (ApiLimiterException e) {
            Assertions.assertEquals("Client * non found for API /api/multiple-clients-test", e.getMessage());
        }

    }
}

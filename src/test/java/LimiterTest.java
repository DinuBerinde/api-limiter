import api.limiter.internal.ApiConfig;
import api.limiter.internal.Limiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LimiterTest {
    private final static String CLIENT = "alportughjl";

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow 5 calls")
    public void shouldAllow5Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test", CLIENT));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should fail consuming 1 call out of 6 calls")
    public void shouldFailOn6Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test", CLIENT));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(CLIENT));
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should allow 8 calls")
    public void shouldAllow8Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test", 8, 5 * 1000, CLIENT));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should fail consuming 2 calls out of 10 calls")
    public void shouldFailOn10Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test",8, 5 * 1000, CLIENT));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(CLIENT));
        Assertions.assertFalse(limiter.consume(CLIENT));
    }


    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 3sec -> Should allow 10 calls: 5 calls + timeframe reset + 5 calls")
    public void shouldAllow10CallsWithTimeframeReset() throws InterruptedException {
        Limiter limiter = new Limiter(new ApiConfig("test", 5, 3 * 1000, CLIENT));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the timeframe reset
            }
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 3sec -> Should fail consuming 2 calls out of 12 calls: 5 calls + timeframe reset + 5 calls + 2 calls")
    public void shouldFailOn12CallsWithTimeframeReset() throws InterruptedException {
        Limiter limiter = new Limiter(new ApiConfig("test", 5, 3 * 1000, CLIENT));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the timeframe reset
            }
            consumers.add(limiter.consume(CLIENT));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(CLIENT));
        Assertions.assertFalse(limiter.consume(CLIENT));
    }
}

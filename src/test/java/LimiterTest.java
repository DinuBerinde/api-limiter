import api.limiter.internal.ApiConfig;
import api.limiter.internal.Limiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LimiterTest {
    private final static String TOKEN = "alportughjl";

    @Test
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should consume 5 calls")
    public void shouldConsume5Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test"));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should fail on consuming 6 calls")
    public void shouldFailOn6Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test"));

        List<Boolean> consumers = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(TOKEN));
    }

    @Test
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should consume 8 calls")
    public void shouldConsume8Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test", 8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should fail on consuming 10 calls")
    public void shouldFailOn10Calls() {
        Limiter limiter = new Limiter(new ApiConfig("test",8, 5 * 1000));

        List<Boolean> consumers = new ArrayList<>(8);
        for (int i = 1; i <= 8; i++) {
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(TOKEN));
        Assertions.assertFalse(limiter.consume(TOKEN));
    }


    @Test
    @DisplayName("MaxCalls = 5, Interval = 3sec -> Should consume 10 calls: 5 calls + interval reset + 5 calls")
    public void shouldConsume10CallsWithIntervalReset() throws InterruptedException {
        Limiter limiter = new Limiter(new ApiConfig("test", 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the interval reset
            }
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
    }

    @Test
    @DisplayName("MaxCalls = 5, Interval = 3sec -> Should fail by consuming 12 calls: 5 calls + interval reset + 5 calls + 2 calls")
    public void shouldFailOn12CallsWithIntervalReset() throws InterruptedException {
        Limiter limiter = new Limiter(new ApiConfig("test", 5, 3 * 1000));

        List<Boolean> consumers = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {

            if (i == 6) {
                Thread.sleep(3 * 1000); // let the interval reset
            }
            consumers.add(limiter.consume(TOKEN));
        }

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(limiter.consume(TOKEN));
        Assertions.assertFalse(limiter.consume(TOKEN));
    }
}

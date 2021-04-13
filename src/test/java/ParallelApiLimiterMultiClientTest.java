import com.dinuberinde.api.limiter.ApiConfig;
import com.dinuberinde.api.limiter.ApiLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelApiLimiterMultiClientTest {
    private final static String API_NAME = "/api/parallel-multi-client-test";
    private final static int NUMBER_OF_THREADS = 5;

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow exactly 25 calls to API for multiple clients")
    public void shouldAllow25Calls() {
        String[] clients = IntStream.range(0, NUMBER_OF_THREADS).mapToObj(String::valueOf).toArray(String[]::new);
        ApiLimiter.registerApis(ApiConfig.of(API_NAME, 5, 10 * 1000, clients));

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS * NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, "1"));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit 1 call out of 26 calls to API for multiple clients")
    public void shouldFailOn26Calls() {
        String[] clients = IntStream.range(0, NUMBER_OF_THREADS).mapToObj(String::valueOf).toArray(String[]::new);
        ApiLimiter.registerApis(ApiConfig.of(API_NAME, 5, 10 * 1000, clients));

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Boolean> consumers = IntStream.range(0, (NUMBER_OF_THREADS * NUMBER_OF_THREADS) + 1)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertFalse(consumers.stream().allMatch(p -> p));
        System.out.println();
    }


    @Test
    @DisplayName("MaxCalls = 50, Timeframe = 5sec -> Should allow 50 calls on API per client")
    public void shouldAllow50Calls() {
        String[] clients = IntStream.range(0, NUMBER_OF_THREADS).mapToObj(String::valueOf).toArray(String[]::new);
        ApiLimiter.registerApis(ApiConfig.of(API_NAME, 50, 10 * 1000, clients));

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS * 10 * NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        Assertions.assertFalse(ApiLimiter.consume(API_NAME, "1"));
        System.out.println();
    }


    private static class WorkerSupplier implements Supplier<Boolean> {
        private final int threadNum;

        public WorkerSupplier(int threadNum) {
            this.threadNum = threadNum % NUMBER_OF_THREADS;
        }

        @Override
        public Boolean get() {
            boolean consumed = ApiLimiter.consume(API_NAME, this.threadNum + "");
            if (consumed)
                System.out.println(String.format("[Task %s] consumed API", threadNum));
            else
                System.out.println(String.format("[Task %s] exceeded API call limit", threadNum));

            return consumed;
        }
    }
}

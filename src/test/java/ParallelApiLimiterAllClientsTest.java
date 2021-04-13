import com.dinuberinde.api.limiter.ApiLimiter;
import com.dinuberinde.api.limiter.ApiConfig;
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


public class ParallelApiLimiterAllClientsTest {
    private final static String API_NAME = "/api/parallel-all-clients-test";
    private final static String CLIENT = "alkmncbvxerop";


    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow 5 calls to API")
    public void shouldAllow5Calls() {
        int NUMBER_OF_THREADS = 5;
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit 1 call out of 6 calls to API")
    public void shouldFailOn6Calls() {
        int NUMBER_OF_THREADS = 6;
        ApiLimiter.registerApis(new ApiConfig(API_NAME));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertFalse(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should allow 8 calls on API")
    public void shouldAllow8Calls() {
        int NUMBER_OF_THREADS = 8;
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 8, Timeframe = 5sec -> Should limit 2 calls out of 10 calls")
    public void shouldFailOn10Calls() {
        int NUMBER_OF_THREADS = 10;
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 8, 5 * 1000));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertFalse(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 50, Timeframe = 5sec -> Should allow 50 calls on API")
    public void shouldAllow50Calls() {
        int NUMBER_OF_THREADS = 50;
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 50, 5 * 1000));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertTrue(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 100, Timeframe = 5sec -> Should limit 1 call out of 101 calls")
    public void shouldFailOn101Calls() {
        int NUMBER_OF_THREADS = 101;
        ApiLimiter.registerApis(new ApiConfig(API_NAME, 100, 5 * 1000));

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertFalse(consumers.stream().allMatch(p -> p));
        System.out.println();
    }

    private static class WorkerSupplier implements Supplier<Boolean> {
        private final int threadNum;

        public WorkerSupplier(int threadNum) {
            this.threadNum = threadNum;
        }

        @Override
        public Boolean get() {
            boolean consumed = this.threadNum % 2 ==0 ? ApiLimiter.consume(API_NAME, CLIENT) : ApiLimiter.consume(API_NAME);
            if (consumed)
                System.out.println(String.format("[Task %s] consumed API", threadNum));
            else
                System.out.println(String.format("[Task %s] exceeded API call limit", threadNum));

            return consumed;
        }
    }
}

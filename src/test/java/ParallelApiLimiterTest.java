import api.limiter.ApiLimiter;
import api.limiter.internal.ApiConfig;
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


public class ParallelApiLimiterTest {
    private final static String API_NAME = "/api/test";
    private final static String TOKEN = "alkmncbvxerop";


    @Test
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should allow 5 calls to api")
    public void shouldAllow5Calls() throws InterruptedException {
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
    @DisplayName("MaxCalls = 5, Interval = 10sec -> Should limit 6 calls to api")
    public void shouldLimit6Calls() {
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
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should allow 8 calls on api")
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
    @DisplayName("MaxCalls = 8, Interval = 5sec -> Should limit 10 calls")
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
    @DisplayName("MaxCalls = 50, Interval = 5sec -> Should allow 50 calls on api")
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
    @DisplayName("MaxCalls = 100, Interval = 5sec -> Should limit 10 calls")
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
            boolean consumed = ApiLimiter.consume(API_NAME, TOKEN);
            if (consumed)
                System.out.println(String.format("[Task %s] consumed api", threadNum));
            else
                System.out.println(String.format("[Task %s] exceeded api call limit", threadNum));

            return consumed;
        }
    }
}

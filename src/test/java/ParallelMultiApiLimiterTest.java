import com.dinuberinde.api.limiter.ApiLimiter;
import com.dinuberinde.api.limiter.internal.ApiConfig;
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

public class ParallelMultiApiLimiterTest {
    private final static int NUMBER_OF_APIS = 5;
    private final static String API_NAME = "/api/test";
    private final static String CLIENT = "alkmncbvxerop";

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow 25 calls to 5 APIs")
    public void shouldAllow25Calls() {
        int NUMBER_OF_THREADS = 25;

        ApiConfig[] apiConfigs = IntStream.range(0, NUMBER_OF_APIS)
                .mapToObj(num -> new ApiConfig(API_NAME + "/" + num))
                .toArray(ApiConfig[]::new);

        ApiLimiter.registerApis(apiConfigs);

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
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit 1 call out of 26 calls to 5 APIs")
    public void shouldFailOn26Calls() {
        int NUMBER_OF_THREADS = 26;

        ApiConfig[] apiConfigs = IntStream.range(0, NUMBER_OF_APIS)
                .mapToObj(num -> new ApiConfig(API_NAME + "/" + num))
                .toArray(ApiConfig[]::new);

        ApiLimiter.registerApis(apiConfigs);

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
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit exactly 1 call out of 26 calls to 5 APIs")
    public void shouldFailOn26CallsFailing1() {
        int NUMBER_OF_THREADS = 26;

        ApiConfig[] apiConfigs = IntStream.range(0, NUMBER_OF_APIS)
                .mapToObj(num -> new ApiConfig(API_NAME + "/" + num))
                .toArray(ApiConfig[]::new);

        ApiLimiter.registerApis(apiConfigs);

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertEquals(1, consumers.stream().filter(p -> !p).count());
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should limit exactly 25 calls out of 50 calls to 5 APIs")
    public void shouldFailOn50Calls() {
        int NUMBER_OF_THREADS = 50;

        ApiConfig[] apiConfigs = IntStream.range(0, NUMBER_OF_APIS)
                .mapToObj(num -> new ApiConfig(API_NAME + "/" + num))
                .toArray(ApiConfig[]::new);

        ApiLimiter.registerApis(apiConfigs);

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertEquals(25, consumers.stream().filter(p -> !p).count());
        System.out.println();
    }

    @Test
    @DisplayName("MaxCalls = 5, Timeframe = 10sec -> Should allow exactly 25 calls out of 38 calls to 5 APIs")
    public void shouldAllowExactly25Calls() {
        int NUMBER_OF_THREADS = 38;

        ApiConfig[] apiConfigs = IntStream.range(0, NUMBER_OF_APIS)
                .mapToObj(num -> new ApiConfig(API_NAME + "/" + num))
                .toArray(ApiConfig[]::new);

        ApiLimiter.registerApis(apiConfigs);

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Boolean> consumers = IntStream.range(0, NUMBER_OF_THREADS)
                .parallel()
                .mapToObj(num -> CompletableFuture.supplyAsync(() -> new WorkerSupplier(num).get(), executor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        Assertions.assertEquals(25, consumers.stream().filter(p -> p).count());
        System.out.println();
    }

    private static class WorkerSupplier implements Supplier<Boolean> {
        private final int threadNum;
        private final int restIndex;

        public WorkerSupplier(int threadNum) {
            this.threadNum = threadNum;
            this.restIndex = threadNum % NUMBER_OF_APIS;
        }

        @Override
        public Boolean get() {
            boolean consumed = ApiLimiter.consume(API_NAME + "/" + restIndex, CLIENT);
            if (consumed)
                System.out.println(String.format("[Task %s] consumed API %s", threadNum, API_NAME + "/" + restIndex));
            else
                System.out.println(String.format("[Task %s] exceeded API %s call limit", threadNum, API_NAME + "/" + restIndex));

            return consumed;
        }
    }
}

package lva.spatialindex.viewer.utils;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
public class ExecutorUtils {
    public static final ExecutorService EXECUTOR_SERVICE;
    static {
        EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MoreExecutors.shutdownAndAwaitTermination(EXECUTOR_SERVICE, 10, TimeUnit.SECONDS);
        }));
    }

    public static <R> Collection<R> gather(Collection<CompletableFuture<R>> asyncResults) {
        return asyncResults.stream().map(CompletableFuture::join)
                .collect(toList());
    }

}
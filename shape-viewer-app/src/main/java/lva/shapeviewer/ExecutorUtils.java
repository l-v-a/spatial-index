package lva.shapeviewer;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
}

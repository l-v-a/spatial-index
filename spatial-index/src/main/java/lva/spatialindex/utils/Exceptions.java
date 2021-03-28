package lva.spatialindex.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * @author vlitvinenko
 */
public class Exceptions {
    private Exceptions() {}

    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;

        default Callable<Void> toCallable() {
            return () -> {
                call();
                return null;
            };
        }
    }

    // TODO: use vavr's CheckedFunctions
    public static void toRuntime(VoidCallable voidCallable) {
        toRuntime(voidCallable.toCallable());
    }

    public static <T> T toRuntime(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception exc) {
            throw runtime(exc);
        }
    }

    public static RuntimeException runtime(Exception exception) {
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        if (exception instanceof RuntimeException) {
            return (RuntimeException)exception;
        }

        if (exception instanceof InvocationTargetException) {
            Throwable cause = exception.getCause();
            if (cause instanceof IOException) {
                return new UncheckedIOException((IOException) cause);
            }
        }

        if (exception instanceof IOException) {
            return new UncheckedIOException((IOException) exception);
        }

        return new RuntimeException(exception);
    }
}

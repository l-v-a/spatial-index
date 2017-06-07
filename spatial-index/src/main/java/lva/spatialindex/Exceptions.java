package lva.spatialindex;

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
    interface VoidCallable {
        void call() throws Exception;
    }

    public static <T> T runtime(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception exc) {
            throw toRuntime(exc);
        }
    }

    public static void runtime(VoidCallable callable) {
        try {
            callable.call();
        } catch (Exception exc) {
            throw toRuntime(exc);
        }
    }

    public static RuntimeException toRuntime(Exception exception) {
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

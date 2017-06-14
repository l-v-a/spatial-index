package lva.shapeviewer;

import java.util.Collection;

/**
 * Eliminates lack of AutoCloseable support in Guava's Closer class
 *
 * @author vlitvinenko
 */
public class AutoCloseables {
    private AutoCloseables() {}

    public static <T extends AutoCloseable> void close(Collection<T> closeables, Exception wasThrown) throws Exception {
        Exception exception = wasThrown;
        for (T closeable: closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    public static <T extends AutoCloseable>  void close(Collection<T> closeables) throws Exception {
        close(closeables, null);
    }
}

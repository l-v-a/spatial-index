package lva.spatialindex;

import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

/**
 * @author vlitvinenko
 */
public class ExceptionsTest {
    @Test
    public void should_return_value_if_all_is_ok() {
        int value = Exceptions.toRuntime(() -> 123);
        assertEquals(123, value);
    }

    @Test
    public void should_throw_original_RuntimeException() {
        RuntimeException exception = new RuntimeException();
        try {
            Exceptions.toRuntime(() -> { throw exception; });
        } catch (RuntimeException exc) {
            assertSame(exception, exc);
            return;
        }
        fail();
    }

    @Test
    public void should_throw_UncheckedIOException_for_InvocationTargetException_if_cause_is_IOException() {
        try {
            Exceptions.toRuntime(() -> { throw new InvocationTargetException(new IOException()); });
        } catch (UncheckedIOException exc) {
            assertTrue(true);
            return;
        }
        fail();
    }

    @Test
    public void should_throw_UncheckedIOException_for_IOException() {
        try {
            Exceptions.toRuntime(() -> { throw new IOException(); });
        } catch (UncheckedIOException exc) {
            assertTrue(true);
            return;
        }
        fail();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_RuntimeIOException_for_unknown_checked_exceptions() {
        Exceptions.toRuntime(() -> {throw new Exception();});
        fail();
    }

}
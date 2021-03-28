package lva.spatialindex.viewer.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;


/**
 * @author vlitvinenko
 */
public abstract class EventSource<E> {
    private final Collection<E> listeners = new ArrayList<>(); // TODO: think about

    public void register(E listener) {
        listeners.add(listener);
    }

    public static class EventStore extends EventSource<Runnable> {
        public void dispatch() {
            dispatch(Runnable::run);
        }
    }

    public static class TypedEventStore<T> extends EventSource<Consumer<T>> {
        public void dispatch(T t) {
            dispatch(c -> c.accept(t));
        }
    }

    protected void dispatch(Consumer<E> visitor) {
        listeners.forEach(visitor);
    }
}

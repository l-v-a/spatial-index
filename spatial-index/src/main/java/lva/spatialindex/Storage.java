package lva.spatialindex;

/**
 * @author vlitvinenko
 */
public interface Storage<T> extends AutoCloseable {
    long add(T t);
    void write(long offset, T t);
    T read(long offset);
    T get(long offset);
    void close();
}

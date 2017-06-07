package lva.spatialindex;

import java.util.function.Function;

/**
 * @author vlitvinenko
 */
public interface StorageSpace extends AutoCloseable {
    int getInt(long pos);
    long getLong(long pos);
    void putInt(long pos, int val);
    void putLong(long pos, long val);
    void getBytes(long pos, byte[] data);
    void putBytes(long pos, byte[] data);

    long allocate(long sizeOf, Function<Long, Long> roundBoundaryFunc);
    long getSize();
    long getCapacity();
    void close();
}

package lva.spatialindex;

/**
 * @author vlitvinenko
 */
public interface StorageSpace extends AutoCloseable {
    int getInt(long pos);
    long getLong(long pos);
    void putInt(long pos, int val);
    void putLong(long pos, long val);
    byte[] getBytes(long pos, int size);
    void putBytes(long pos, byte[] data);

    long allocate(long sizeOf);
    long getSize();
    long getCapacity();
    void close();
}

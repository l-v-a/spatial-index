package lva.spatialindex.storage;

/**
 * @author vlitvinenko
 */
public interface StorageSpace extends AutoCloseable {
    byte[] readBytes(long pos, int size);
    void readBytes(long pos, byte[] buff);
    void writeBytes(long pos, byte[] data);
    long allocate(long sizeOf);
    long getSize();
    long getCapacity();
    void close();
}

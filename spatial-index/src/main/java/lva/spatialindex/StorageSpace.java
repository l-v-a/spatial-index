package lva.spatialindex;

/**
 * @author vlitvinenko
 */
public interface StorageSpace extends AutoCloseable {
    byte[] readBytes(long pos, int size);
    void writeBytes(long pos, byte[] data);
    long allocate(long sizeOf);
    long getSize();
    long getCapacity();
    void close();
}

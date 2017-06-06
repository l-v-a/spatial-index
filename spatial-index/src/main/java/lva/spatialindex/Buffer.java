package lva.spatialindex;

/**
 * @author vlitvinenko
 */

// TODO: rename to Region
public interface Buffer {
    int getInt(long pos);
    long getLong(long pos);
    void putInt(long pos, int val);
    void putLong(long pos, long val);

    long allocate(long sizeOf) throws Exception;
}

package lva.spatialindex.storage;

/**
 * @author vlitvinenko
 */

public abstract class AbstractStorage<T> implements Storage<T>{
    protected interface Serializer<T> {
        byte[] serialize(T t);
        T deserialize(byte[] bytes);
    }

    private final int recordSize;
    private final StorageSpace storageSpace;

    public AbstractStorage(StorageSpace storageSpace, int recordSize) {
        this.storageSpace = storageSpace;
        this.recordSize = recordSize;
    }

    protected abstract Serializer<T> getSerializer();

    @Override
    public long add(T t) {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        long offset = storageSpace.allocate(roundToRecordSize(buff.length));
        storageSpace.writeBytes(offset, buff);
        return offset;

    }

    @Override
    public void write(long offset, T t) {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        if (offset < 0 || offset + buff.length > storageSpace.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }

        storageSpace.writeBytes(offset, buff);
    }

    @Override
    public T read(long offset) {
        if (offset + recordSize > storageSpace.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }

        byte[] buff = storageSpace.readBytes(offset, recordSize);
        return getSerializer().deserialize(buff);
    }

    @Override
    public void close() {
        this.storageSpace.close();
    }

    private long roundToRecordSize(long size) {
        return (size + (recordSize - 1)) & ~(recordSize - 1);
    }
}


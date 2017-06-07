package lva.spatialindex;

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

    public AbstractStorage(String fileName, long initialSize, int recordSize) {
        this(new MemoryMappedFile(fileName, initialSize), recordSize);
    }

    AbstractStorage(StorageSpace storageSpace, int recordSize) {
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

        long offset = storageSpace.allocate(buff.length, (x) -> (x + (recordSize - 1)) & ~(recordSize - 1));
        storageSpace.putBytes(offset, buff);
        return offset;

    }

    @Override
    public void write(long offset, T t) {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("record was not allocated");
        }

        storageSpace.putBytes(offset, buff);
    }

    @Override
    public T read(long offset) {
        byte[] buff = new byte[recordSize];
        if (offset + buff.length > storageSpace.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }
        storageSpace.getBytes(offset, buff);
        return getSerializer().deserialize(buff);
    }

    @Override
    public T get(long offset) {
        return read(offset);
    }

    @Override
    public void close() {
        this.storageSpace.close();
    }

}


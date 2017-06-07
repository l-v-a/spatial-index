package lva.spatialindex;

/**
 * @author vlitvinenko
 */

abstract class AbstractStorage<T> implements Storage<T>{
    interface Serializer<T> {
        byte[] serialize(T t);
        T deserialize(byte[] bytes);
    }

    private final int recordSize;
    private final MemoryMappedFile storage;

    public AbstractStorage(String fileName, long initialSize, int recordSize) {
        this.storage = new MemoryMappedFile(fileName, initialSize);
        this.recordSize = recordSize;
    }

    abstract Serializer<T> getSerializer();

    @Override
    public long add(T t) {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        long offset = storage.allocate(buff.length, (x) -> (x + (recordSize - 1)) & ~(recordSize - 1));
        storage.putBytes(offset, buff);
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

        storage.putBytes(offset, buff);
    }

    @Override
    public T read(long offset) {
        byte[] buff = new byte[recordSize];
        if (offset + buff.length > storage.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }
        storage.getBytes(offset, buff);
        return getSerializer().deserialize(buff);
    }

    @Override
    public T get(long offset) {
        return read(offset);
    }

    @Override
    public void close() {
        this.storage.close();
    }

}


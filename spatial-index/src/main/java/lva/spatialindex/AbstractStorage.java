package lva.spatialindex;

import java.io.IOException;

/**
 * @author vlitvinenko
 */

abstract class AbstractStorage<T> implements AutoCloseable {
    interface Serializer<T> {
        byte[] serialize(T t) throws IOException;
        T deserialize(byte[] bytes) throws IOException;
    }

    private final int recordSize;
    private final MemoryMappedFile storage;

    public AbstractStorage(String fileName, long initialSize, int recordSize) throws Exception {
        this.storage = new MemoryMappedFile(fileName, initialSize);
        this.recordSize = recordSize;
    }

    abstract Serializer<T> getSerializer();

    public long add(T t) throws Exception {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        long offset = storage.allocate(buff.length, (x) -> (x + (recordSize - 1)) & ~(recordSize - 1));
        storage.setBytes(offset, buff);
        return offset;

    }

    public void write(long offset, T t) throws Exception {
        byte[] buff = getSerializer().serialize(t);
        if (buff.length > recordSize) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("record was not allocated");
        }

        storage.setBytes(offset, buff);
    }

    public T read(long offset) throws Exception {
        byte[] buff = new byte[recordSize];
        if (offset + buff.length > storage.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }
        storage.getBytes(offset, buff);
        return getSerializer().deserialize(buff);
    }

    public T get(long offset) throws Exception {
        return read(offset);
    }

    @Override
    public void close() throws Exception {
        this.storage.close();
    }

}

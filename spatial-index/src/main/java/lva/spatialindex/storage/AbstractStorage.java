package lva.spatialindex.storage;

import lva.spatialindex.utils.Exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author vlitvinenko
 */
public abstract class AbstractStorage<T> implements Storage<T> {

    protected interface Serializer<T> {
        byte[] serialize(T t);
        T deserialize(byte[] bytes);
    }

    abstract protected static class AbstractSerializer<T> implements Serializer<T> {
        @Override
        public byte[] serialize(T t) {
            return Exceptions.toRuntime(() -> {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    write(os, t);
                    return os.toByteArray();
                }
            });
        }

        @Override
        public T deserialize(byte[] bytes) {
            return Exceptions.toRuntime(() -> {
                try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
                    return read(is);
                }
            });
        }

        abstract public void write(OutputStream os, T t) throws Exception;
        abstract public T read(InputStream is) throws Exception;
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
        checkArgument(buff.length <= recordSize, "record max size exceeds");

        long offset = storageSpace.allocate(roundToRecordSize(buff.length));
        storageSpace.writeBytes(offset, buff);
        return offset;

    }

    @Override
    public void write(long offset, T t) {
        byte[] buff = getSerializer().serialize(t);
        checkArgument(buff.length <= recordSize, "record max size exceeds");
        checkArgument(0 <= offset && offset + buff.length <= storageSpace.getSize(), "out of bounds");

        storageSpace.writeBytes(offset, buff);
    }

    @Override
    public T read(long offset) {
        checkArgument(offset + recordSize <= storageSpace.getSize(), "out of bounds");

        byte[] buff = storageSpace.readBytes(offset, recordSize);
        return getSerializer().deserialize(buff);
    }

    @Override
    public void close() {
        this.storageSpace.close();
    }

    private long roundToRecordSize(long size) {
        return (size + (recordSize - 1)) & -recordSize;
    }
}


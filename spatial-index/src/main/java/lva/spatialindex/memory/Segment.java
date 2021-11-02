package lva.spatialindex.memory;

import lva.spatialindex.storage.StorageSpace;
import lva.spatialindex.utils.Exceptions;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author vlitvinenko
 */
class Segment implements StorageSpace  {
    static int PAGE_SIZE = 4096;

    private long size;
    private final long capacity;
    private final String filePath;

    private final ThreadLocal<ByteBuffer> dataTLS;

    public Segment(String filePath, long capacity) {
        this.filePath = filePath;
        this.capacity = roundToPage(capacity);

        ByteBuffer data = mapBackingFile(this.filePath, this.capacity);
        this.dataTLS = ThreadLocal.withInitial(data::duplicate);
    }

    @Override
    public byte[] readBytes(long pos, int size) {
        byte [] data = new byte[size];
        readBytes(pos, data);
        return data;
    }

    @Override
    public void readBytes(long pos, byte[] buff) {
        dataTLS.get()
                .position((int) pos)
                .get(buff);
    }

    @Override
    public void writeBytes(long pos, byte[] buff) {
        dataTLS.get()
                .position((int) pos)
                .put(buff);
    }

    @Override
    public long allocate(long sizeOf) {
        checkArgument(size + sizeOf <= capacity, String.format("Out of segment space. " +
                        "capacity: %d, size: %d, sizeOf: %d", capacity, size, sizeOf));

        long offset = size;
        size += sizeOf;
        return offset;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    String getFilePath() {
        return filePath;
    }

    @Override
    public void clear() {
        size = 0;
    }

    private static ByteBuffer mapBackingFile(String filePath, long capacity) {
        return Exceptions.toRuntime(() -> {
            try (RandomAccessFile backingFile = new RandomAccessFile(filePath, "rw")) {
                backingFile.setLength(capacity);
                try (FileChannel channel = backingFile.getChannel()) {
                    return channel.map(MapMode.READ_WRITE, 0L, capacity);
                }
            }
        });
    }

    private static long roundToPage(long i) {
        return (i + (PAGE_SIZE - 1)) & -PAGE_SIZE;
    }
}

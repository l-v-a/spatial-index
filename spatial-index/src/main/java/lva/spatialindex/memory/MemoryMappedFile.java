package lva.spatialindex.memory;

import lva.spatialindex.Exceptions;
import lva.spatialindex.StorageSpace;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static lva.spatialindex.memory.unsafe.Native.*;


/**
 * @author vlitvinenko
 */
public class MemoryMappedFile implements StorageSpace {
    static int PAGE_SIZE = 4096;

    private long baseAddress;
    private long capacity;
    private long size;

    private final String filePath;

    public MemoryMappedFile(String filePath, long capacity) {
        this.filePath = filePath;
        this.capacity = roundToPage(capacity);
        mapBackingFile();
    }

    @Override
    public byte[] readBytes(long pos, int size) {
        byte [] data = new byte[size];
        UNSAFE.copyMemory(null, baseAddress + pos, data, BYTE_ARRAY_OFFSET, data.length);
        return data;
    }

    @Override
    public void writeBytes(long pos, byte[] data) {
       UNSAFE.copyMemory(data, BYTE_ARRAY_OFFSET, null, baseAddress + pos, data.length);
    }

    @Override
    public long allocate(long sizeOf) {
        long offset = size;
        while (offset + sizeOf > getCapacity())  {
            remap(capacity + (capacity >> 1));
        }

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

    @Override
    public void close() {
        Exceptions.toRuntime(() -> {
            unmap(baseAddress, capacity);
        });
    }

    private void mapBackingFile() {
        Exceptions.toRuntime(() -> {
            try (RandomAccessFile backingFile = new RandomAccessFile(filePath, "rw")) {
                backingFile.setLength(capacity);
                try (FileChannel channel = backingFile.getChannel()) {
                    baseAddress = map(channel, MAP_RW, 0L, capacity);
                }
            }
        });
    }

    private void remap(long length) {
        Exceptions.toRuntime(() -> {
            unmap(baseAddress, capacity);
            capacity = roundToPage(length);
            mapBackingFile();
        });
    }

    private static long roundToPage(long i) {
        return (i + (PAGE_SIZE - 1)) & ~(PAGE_SIZE - 1);
    }
}


package lva.spatialindex;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;

import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.function.Function;


/**
 * @author vlitvinenko
 */
public class MemoryMappedFile implements StorageSpace {

    private static final Unsafe unsafe;
    private static final Method mmap;
    private static final Method unmmap;
    private static final int BYTE_ARRAY_OFFSET;

    // TODO: think about
    volatile long addr, capacity;
    volatile long size;
    private final String loc;


    static {
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe) singleoneInstanceField.get(null);

            mmap = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            unmmap = getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);

            BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        } catch (Exception e){
            throw Exceptions.toRuntime(e);
        }

    }

    public MemoryMappedFile(String loc, long len) {
        this.loc = loc;
        this.capacity = roundTo4096(len);
        mapAndSetOffset();
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    private static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }

    private void mapAndSetOffset() {
        Exceptions.runtime(() -> {
            // TODO: hold the files
            try (RandomAccessFile backingFile = new RandomAccessFile(this.loc, "rw")) {
                backingFile.setLength(this.capacity);
                try (FileChannel ch = backingFile.getChannel()) {
                    this.addr = (long) mmap.invoke(ch, 1, 0L, this.capacity);
                }
            }
        });
    }

    private void remap(long nLen) {
        Exceptions.runtime(() -> {
            unmmap.invoke(null, addr, this.capacity);
            this.capacity = roundTo4096(nLen);
            mapAndSetOffset();
        });
    }

    @Override
    public void close() {
        Exceptions.runtime(() -> {
            unmmap.invoke(null, addr, this.capacity);
        });
    }

    @Override
    public void getBytes(long pos, byte[] data) {
        unsafe.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET, data.length);
    }

    @Override
    public void putBytes(long pos, byte[] data) {
        unsafe.copyMemory(data, BYTE_ARRAY_OFFSET, null, pos + addr, data.length);
    }

    @Override
    public long allocate(long sizeOf, Function<Long, Long> roundBoundaryFunc) {
        long sizeOfRounded = roundBoundaryFunc.apply(sizeOf);
        long offset = size;

        while (offset + sizeOfRounded > capacity)  {
            remap(capacity * 2);
        }

        size += sizeOfRounded;
        return offset;
    }

    @Override
    public int getInt(long pos) {
        return unsafe.getInt(addr + pos);
    }

    @Override
    public long getLong(long pos) {
        return unsafe.getLong(addr + pos);
    }

    @Override
    public void putInt(long pos, int val) {
        unsafe.putInt(addr + pos, val);
    }

    @Override
    public void putLong(long pos, long val) {
        unsafe.putLong(addr + pos, val);
    }

    @Override
    public long getSize() {
        return size;
    }

    public long getCapacity() {
        return capacity;
    }


}


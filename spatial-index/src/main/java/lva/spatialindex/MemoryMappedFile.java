package lva.spatialindex;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;

import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;


/**
 * @author vlitvinenko
 */
public class MemoryMappedFile implements StorageSpace {

    private static final Unsafe unsafe;
    private static final Method mmapMethod;
    private static final Method unmmapMethod;
    private static final int BYTE_ARRAY_OFFSET;

    // TODO: think about
    private volatile long baseAddress;
    private volatile long capacity;
    private volatile long size;

    private final String filePath;


    static {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);

            mmapMethod = getMethod(FileChannelImpl.class, "map0", int.class, long.class, long.class);
            unmmapMethod = getMethod(FileChannelImpl.class, "unmap0", long.class, long.class);

            BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        } catch (Exception e){
            throw Exceptions.toRuntime(e);
        }

    }

    public MemoryMappedFile(String filePath, long size) {
        this.filePath = filePath;
        this.capacity = roundTo4096(size);
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
            try (RandomAccessFile backingFile = new RandomAccessFile(this.filePath, "rw")) {
                backingFile.setLength(this.capacity);
                try (FileChannel ch = backingFile.getChannel()) {
                    this.baseAddress = (long) mmapMethod.invoke(ch, 1, 0L, this.capacity);
                }
            }
        });
    }

    private void remap(long nLen) {
        Exceptions.runtime(() -> {
            unmmapMethod.invoke(null, baseAddress, this.capacity);
            this.capacity = roundTo4096(nLen);
            mapAndSetOffset();
        });
    }

    @Override
    public void close() {
        Exceptions.runtime(() -> {
            unmmapMethod.invoke(null, baseAddress, this.capacity);
        });
    }

    @Override
    public byte[] getBytes(long pos, int size) {
        byte [] data = new byte[size];
        unsafe.copyMemory(null, baseAddress + pos, data, BYTE_ARRAY_OFFSET, data.length);
        return data;
    }

    @Override
    public void putBytes(long pos, byte[] data) {
        unsafe.copyMemory(data, BYTE_ARRAY_OFFSET, null, baseAddress + pos, data.length);
    }

    @Override
    public long allocate(long sizeOf) {
        long offset = size;
        while (offset + sizeOf > getCapacity())  {
            remap(getCapacity() * 2);
        }

        size += sizeOf;
        return offset;
    }

    @Override
    public int getInt(long pos) {
        return unsafe.getInt(baseAddress + pos);
    }

    @Override
    public long getLong(long pos) {
        return unsafe.getLong(baseAddress + pos);
    }

    @Override
    public void putInt(long pos, int val) {
        unsafe.putInt(baseAddress + pos, val);
    }

    @Override
    public void putLong(long pos, long val) {
        unsafe.putLong(baseAddress + pos, val);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

}


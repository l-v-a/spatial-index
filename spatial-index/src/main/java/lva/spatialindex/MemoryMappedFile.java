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

// TODO: think about to implement Buffer
public class MemoryMappedFile implements AutoCloseable, Buffer {

    private static final Unsafe unsafe;
    private static final Method mmap;
    private static final Method unmmap;
    private static final int BYTE_ARRAY_OFFSET;

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
            throw new RuntimeException(e);
        }
    }

    // TODO: think about to use stream
    public static class DirectArray implements AutoCloseable {
        // bounds are not checked for performance reasons
        private final long startIndex;
        private final long size;

        public DirectArray(long size) {
            startIndex = unsafe.allocateMemory(size);
            // unsafe.setMemory(startIndex, size, (byte)0);
            this.size = size;
        }


        public DirectArray(byte[] buff) {
            startIndex = unsafe.allocateMemory(buff.length);
            unsafe.copyMemory(buff, BYTE_ARRAY_OFFSET, null, startIndex, buff.length);
            this.size = buff.length;
        }


        public void putInt(long index, int value) {
            unsafe.putInt(index(index), value);
        }

        public int getInt(long index) {
            return unsafe.getInt(index(index));
        }

        public void putLong(long index, long value) {
            unsafe.putLong(index(index), value);
        }

        public long getLong(long index) {
            return unsafe.getLong(index(index));
        }

        private long index(long offset) {
            return startIndex + offset;
        }

        @Override
        public void close() throws Exception {
            unsafe.freeMemory(startIndex);
        }

        public long getAddr() {
            return startIndex;
        }

        public long getSize() {
            return size;
        }
    }

    public static class DirectOutputStream {
        private final DirectArray buff;
        int pos = 0;
        public DirectOutputStream(DirectArray buff) {
            this.buff = buff;
        }

        public void writeInt(int value) {
            buff.putInt(pos, value);
            pos += 4;
        }

        public void writeLong(long value) {
            buff.putLong(pos, value);
            pos += 8;
        }
    }

    public static class DirectInputStream {
        private final DirectArray buff;
        int pos = 0;
        public DirectInputStream(DirectArray buff) {
            this.buff = buff;
        }

        public int readInt() {
            int value = buff.getInt(pos);
            pos += 4;
            return value;
        }

        public long readLong() {
            long value = buff.getLong(pos);
            pos += 8;
            return value;
        }
    }

    //Bundle reflection calls to get access to the given method
    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws Exception {
        Method m = cls.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    //Round to next 4096 bytes
    private static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }

    private void mapAndSetOffset() throws Exception {
        // TODO: hold the files
        try (RandomAccessFile backingFile = new RandomAccessFile(this.loc, "rw")) {
            backingFile.setLength(this.capacity);
            try (FileChannel ch = backingFile.getChannel()) {
                this.addr = (long) mmap.invoke(ch, 1, 0L, this.capacity);
            }
        }
    }

    public MemoryMappedFile(String loc, long len) throws Exception {
        this.loc = loc;
        this.capacity = roundTo4096(len);
        mapAndSetOffset();
    }

    //Callers should synchronize to avoid calls in the middle of this, but
    //it is undesirable to synchronize w/ all access methods.
    private void remap(long nLen) throws Exception {
        unmmap.invoke(null, addr, this.capacity);
        this.capacity = roundTo4096(nLen);
        mapAndSetOffset();
    }

    @Override
    public void close() throws Exception {
        unmmap.invoke(null, addr, this.capacity);
    }

    //May want to have offset & length within data as well, for both of these
    public void getBytes(long pos, byte[] data){
        unsafe.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET, data.length);
    }

    public void setBytes(long pos, byte[] data){
        unsafe.copyMemory(data, BYTE_ARRAY_OFFSET, null, pos + addr, data.length);
    }

    public void setArray(long pos, DirectArray array){
        unsafe.copyMemory(array.getAddr(), pos + addr, array.getSize());
    }

    public DirectArray getArray(long pos, long size) {
        DirectArray array = new DirectArray(size);
        unsafe.copyMemory(pos + addr, array.getAddr(), array.getSize());
        return array;
    }

    public void getArray(long pos, DirectArray array) {
        unsafe.copyMemory(pos + addr, array.getAddr(), array.getSize());
    }

    @Override
    public long allocate(long sizeOf) throws Exception {
//        long sizeOfRounded = roundTo4096(sizeOf);
//        long offset = size;
//
//        while (offset + sizeOfRounded > capacity)  {
//            remap(capacity * 2);
//        }
//
//        size += sizeOfRounded;
//        return offset;
        return allocate(sizeOf, MemoryMappedFile::roundTo4096);
    }



    public long allocate(long sizeOf, Function<Long, Long> roundBoundaryFunc) throws Exception {
        long sizeOfRounded = roundBoundaryFunc.apply(sizeOf);
        long offset = size;

        while (offset + sizeOfRounded > capacity)  {
            remap(capacity * 2);
        }

        size += sizeOfRounded;
        return offset;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getSize() {
        return size;
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
}


package lva.spatialindex.memory.unsafe;

import lva.spatialindex.utils.Exceptions;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

/**
 * @author vlitvinenko
 */
public class Native {
    private Native() {}

    public static final Unsafe UNSAFE;
    public static final int BYTE_ARRAY_OFFSET;

    public static final int MAP_RO = 0;
    public static final int MAP_RW = 1;
    public static final int MAP_PV = 2;

    private static final Method MAP_METHOD;
    private static final Method UNMAP_METHOD;

    static {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafeField.get(null);

            Class<?> fciClass = Class.forName("sun.nio.ch.FileChannelImpl");
            MAP_METHOD = getMethod(fciClass, "map0", int.class, long.class, long.class);
            UNMAP_METHOD = getMethod(fciClass, "unmap0", long.class, long.class);

            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e){
            throw Exceptions.runtime(e);
        }
    }

    public static int unmap(long address, long length) throws InvocationTargetException, IllegalAccessException {
        return (int) UNMAP_METHOD.invoke(null, address, length);
    }

    public static long map(FileChannel channel, int prot, long position, long length) throws InvocationTargetException, IllegalAccessException {
        return (long) MAP_METHOD.invoke(channel, prot, position, length);
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>... params) throws NoSuchMethodException {
        Method method = cls.getDeclaredMethod(name, params);
        method.setAccessible(true);
        return method;
    }
}

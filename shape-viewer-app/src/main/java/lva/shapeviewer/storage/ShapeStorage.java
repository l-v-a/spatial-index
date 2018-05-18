package lva.shapeviewer.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.NonNull;
import lva.spatialindex.memory.MemoryMappedFile;
import lva.spatialindex.storage.AbstractStorage;
import lva.spatialindex.utils.Exceptions;

import java.awt.*;
import java.io.ByteArrayOutputStream;

/**
 * @author vlitvinenko
 */

public class ShapeStorage extends AbstractStorage<Shape> {
    private static class ShapeSerializer implements Serializer<Shape> {
        private final Kryo kryo = new Kryo();
        {
            kryo.register(Shape.class);
            kryo.register(AbstractShape.class);
            kryo.register(RectangleShape.class);
            kryo.register(CircleShape.class);
            kryo.register(Rectangle.class);
        }

        @NonNull
        @Override
        public byte[] serialize(@NonNull Shape shape) {
            return Exceptions.toRuntime(() -> {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     Output output = new Output(baos);) {

                    kryo.writeClassAndObject(output, shape);
                    output.flush();
                    return baos.toByteArray();
                }
            });
        }

        @NonNull
        @Override
        public Shape deserialize(@NonNull byte[] bytes) {
            try (Input input = new Input(bytes)) {
                return (Shape) kryo.readClassAndObject(input);
            }
        }
    }

    private static final int RECORD_SIZE = 128;
    private final ShapeSerializer serializer = new ShapeSerializer();

    @NonNull
    @Override
    protected Serializer<Shape> getSerializer() {
        return serializer;
    }

    public ShapeStorage(@NonNull String fileName, long initialSize) throws Exception {
        super(new MemoryMappedFile(fileName, initialSize), RECORD_SIZE);

    }

    @Override
    public long add(@NonNull Shape shape) {
        // TODO: think about interface and move to base class
        long offset = super.add(shape);
        shape.setOffset(offset);
        return offset;
    }

    @NonNull
    @Override
    public Shape read(long offset) {
        // TODO: think about interface and move to base class
        Shape shape = super.read(offset);
        shape.setOffset(offset);
        return shape;
    }
}
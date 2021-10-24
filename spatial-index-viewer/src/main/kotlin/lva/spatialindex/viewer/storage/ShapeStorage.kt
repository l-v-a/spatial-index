package lva.spatialindex.viewer.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.NonNull;
import lva.spatialindex.memory.MemoryMappedFile;
import lva.spatialindex.storage.AbstractStorage;

import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author vlitvinenko
 */
public class ShapeStorage extends AbstractStorage<Shape> {

    private static class ShapeSerializer extends AbstractSerializer<Shape> {
        private final Kryo kryo = new Kryo();
        {
            kryo.register(Shape.class);
            kryo.register(AbstractShape.class);
            kryo.register(RectangleShape.class);
            kryo.register(CircleShape.class);
            kryo.register(Rectangle.class);
        }

        @Override
        public void write(OutputStream os, Shape shape) {
            try (Output output = new Output(os)) {
                kryo.writeClassAndObject(output, shape);
                output.flush();
            }
        }

        @Override
        public Shape read(InputStream is) {
            try (Input input = new Input(is)) {
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
        long offset = super.add(shape);
        shape.setOffset(offset);
        return offset;
    }

    @NonNull
    @Override
    public Shape read(long offset) {
        Shape shape = super.read(offset);
        shape.setOffset(offset);
        return shape;
    }
}
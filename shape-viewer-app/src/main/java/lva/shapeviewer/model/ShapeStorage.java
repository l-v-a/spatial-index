package lva.shapeviewer.model;

import lombok.NonNull;
import lva.spatialindex.AbstractStorage;
import lva.spatialindex.memory.MemoryMappedFile;

/**
 * @author vlitvinenko
 */

public class ShapeStorage extends AbstractStorage<Shape> {
    private static class ShapeSerializer implements Serializer<Shape> {
        @NonNull
        @Override
        public byte[] serialize(@NonNull Shape shape) {
            return Shape.serialize(shape);
        }

        @NonNull
        @Override
        public Shape deserialize(@NonNull byte[] bytes) {
            return Shape.deserialize(bytes);
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
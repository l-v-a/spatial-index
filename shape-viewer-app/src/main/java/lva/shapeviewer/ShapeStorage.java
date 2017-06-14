package lva.shapeviewer;

import lva.spatialindex.AbstractStorage;
import lva.spatialindex.memory.MemoryMappedFile;

/**
 * @author vlitvinenko
 */
public class ShapeStorage extends AbstractStorage<Shape> {
    private static final int RECORD_SIZE = 128;
    private static class ShapeSerializer implements Serializer<Shape> {

        @Override
        public byte[] serialize(Shape shape) {
            return Shape.serialize(shape);
        }

        @Override
        public Shape deserialize(byte[] bytes) {
            return Shape.deserialize(bytes);
        }
    }

    private final ShapeSerializer serializer = new ShapeSerializer();

    @Override
    protected Serializer<Shape> getSerializer() {
        return serializer;
    }

    public ShapeStorage(String fileName, long initialSize) throws Exception {
        super(new MemoryMappedFile(fileName, initialSize), RECORD_SIZE);
    }

    @Override
    public long add(Shape shape) {
        // TODO: think about interface and move to base class
        long offset = super.add(shape);
        shape.setOffset(offset);
        return offset;
    }

    @Override
    public Shape read(long offset) {
        // TODO: think about interface and move to base class
        Shape shape = super.read(offset);
        shape.setOffset(offset);
        return shape;
    }
}


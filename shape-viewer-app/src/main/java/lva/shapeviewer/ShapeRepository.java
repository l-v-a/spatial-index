package lva.shapeviewer;

import lva.spatialindex.Exceptions;
import lva.spatialindex.Storage;
import lva.spatialindex.index.Index;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author vlitvinenko
 */
public class ShapeRepository implements AutoCloseable {
    private final Storage<Shape> shapeStorage;
    private final Index index;

    public ShapeRepository(Storage<Shape> shapeStorage, Index index) {
        this.shapeStorage = shapeStorage;
        this.index = index;
    }

    public List<Shape> search(Rectangle area) {
        List<Shape> shapes = new ArrayList<>();
        for (long id : index.search(area)) {
            Shape shape = shapeStorage.read(id);
            shapes.add(shape);
        }
        return shapes;
    }

    public void update(Shape shape) {
        shapeStorage.write(shape.getOffset(), shape);
    }

    @Override
    public void close() {
        Exceptions.toRuntime(() -> AutoCloseables.close(asList(index, shapeStorage)));
    }
}

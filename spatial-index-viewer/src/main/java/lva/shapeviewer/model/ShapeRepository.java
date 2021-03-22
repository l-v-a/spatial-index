package lva.shapeviewer.model;

import lombok.NonNull;
import lva.shapeviewer.storage.Shape;
import lva.shapeviewer.utils.AutoCloseables;
import lva.spatialindex.index.Index;
import lva.spatialindex.storage.Storage;
import lva.spatialindex.utils.Exceptions;

import java.awt.*;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
public class ShapeRepository implements AutoCloseable {
    private final Storage<Shape> shapeStorage;
    private final Index index;

    public ShapeRepository(@NonNull Storage<Shape> shapeStorage, @NonNull Index index) {
        this.shapeStorage = shapeStorage;
        this.index = index;
    }

    @NonNull
    public List<Shape> search(@NonNull Rectangle area) {
        return index.search(area).stream().map(shapeStorage::read).collect(toList());
    }

    public void update(@NonNull Shape shape) {
        shapeStorage.write(shape.getOffset(), shape);
    }

    @Override
    public void close() {
        Exceptions.toRuntime(() -> AutoCloseables.close(asList(index, shapeStorage)));
    }
}

package lva.spatialindex.viewer.controller;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.storage.Shape;
import lva.spatialindex.viewer.ui.ShapesFrame;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author vlitvinenko
 */
public class ShapesViewController implements ShapesFrame.ShapesViewListener {
    private final ShapesFrame view;
    private final ShapeRepository shapeRepository;
    private List<lva.spatialindex.viewer.storage.Shape> visibleShapes;

    public ShapesViewController(@NonNull ShapesFrame view, @NonNull ShapeRepository shapeRepository) {
        this.view = view;
        this.view.setViewListener(this);
        this.shapeRepository = shapeRepository;
    }

    public void run() {
        this.view.setVisible(true);
    }

    @Override
    public void clicked(@NonNull MouseEvent event) {
        Rectangle viewport = view.getViewport();
        // translate to viewport
        int x = event.getX() + viewport.x;
        int y = event.getY() + viewport.y;

        lva.spatialindex.viewer.storage.Shape clickedShape = null;
        Iterator<lva.spatialindex.viewer.storage.Shape> shapeIterator = Lists.reverse(visibleShapes).iterator();
        while (shapeIterator.hasNext()) {
            lva.spatialindex.viewer.storage.Shape shape = shapeIterator.next();
            if (shape.hitTest(x, y)) {
                clickedShape = shape;
                break;
            }
        }

        if (clickedShape == null) {
            // nothing to do
            return;
        }

        clickedShape.setActive(!clickedShape.isActive());
        clickedShape.setOrder(clickedShape.getMaxOrder() + 1);
        clickedShape.setMaxOrder(clickedShape.getOrder());

        shapeRepository.update(clickedShape);

        // push to back with highest order
        shapeIterator.remove();
        visibleShapes.add(clickedShape);

        view.update();

    }

    @Override
    public void viewPortChanged() {
        Rectangle viewport = view.getViewport();
        visibleShapes = shapeRepository.search(viewport);
        Collections.sort(visibleShapes, Comparator.comparing(Shape::getOrder, Integer::compare));

        view.setShapes(visibleShapes);
        view.update();
    }

    @Override
    public void closing() {
        shapeRepository.close();
    }
}

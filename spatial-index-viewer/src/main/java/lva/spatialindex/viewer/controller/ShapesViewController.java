package lva.spatialindex.viewer.controller;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lva.spatialindex.viewer.repository.ShapeRepository;
import lva.spatialindex.viewer.storage.Shape;
import lva.spatialindex.viewer.ui.ShapesFrame;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author vlitvinenko
 */
public class ShapesViewController {
    private final ShapesFrame view;
    private final ShapeRepository shapeRepository;
    private List<Shape> visibleShapes;

    public ShapesViewController(@NonNull ShapesFrame view, @NonNull ShapeRepository shapeRepository) {
        this.view = view;
        this.shapeRepository = shapeRepository;

        this.view.clickedEvent.register(this::onShapesViewClicked);
        this.view.closingEvent.register(this::onClosing);
        this.view.viewPortChangedEvent.register(this::onViewPortChanged);
    }

    public void run() {
        this.view.setVisible(true);
    }

    private void onShapesViewClicked(MouseEvent event) {
        Rectangle viewport = view.getViewport();
        int x = event.getX() + viewport.x;
        int y = event.getY() + viewport.y;

        takeClicked(x, y).ifPresent(clickedShape -> {
            // push to back with the highest order
            clickedShape.setActive(!clickedShape.isActive());
            clickedShape.setOrder(clickedShape.getMaxOrder() + 1);
            clickedShape.setMaxOrder(clickedShape.getOrder());

            shapeRepository.update(clickedShape);
            visibleShapes.add(clickedShape);

            view.update();
        });
    }

    private Optional<Shape> takeClicked(int x, int y) {
        Iterator<Shape> shapeIterator = Lists.reverse(visibleShapes).iterator();
        while (shapeIterator.hasNext()) {
            Shape shape = shapeIterator.next();
            if (shape.hitTest(x, y)) {
                shapeIterator.remove();
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }

    private void onViewPortChanged() {
        Rectangle viewport = view.getViewport();
        visibleShapes = shapeRepository.search(viewport);
        visibleShapes.sort(Comparator.comparing(Shape::getOrder, Integer::compare));

        view.setShapes(visibleShapes);
        view.update();
    }

    private void onClosing() {
        shapeRepository.close();
    }
}

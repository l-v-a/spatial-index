package lva.shapeviewer.controller;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lva.shapeviewer.model.Shape;
import lva.shapeviewer.model.ShapeRepository;
import lva.shapeviewer.ui.ShapesFrame;

import java.awt.Rectangle;
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
    private List<Shape> visibleShapes;

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

        Shape clickedShape = null;
        Iterator<Shape> shapeIterator = Lists.reverse(visibleShapes).iterator();
        while (shapeIterator.hasNext()) {
            Shape shape = shapeIterator.next();
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
// for reorder
//        if (shapes.size() > 1) {
//            // TODO: BUG: must use maxOrder over all elements
//            clickedShape.setOrder(Iterables.getLast(shapes).getOrder() + 1); // TODO: think about overflow
//        }

        shapeRepository.update(clickedShape);

        // push to back with highest order
// for reorder
//        shapeIterator.remove();
//        shapes.add(clickedShape);

        view.update();


//        // TODO: BUG: must store reference to active shapes
//        // TODO: think about to use LinkedList for shapes
//        // find shape
//        Shape clickedShape = null;
//        List<Shape> activeShapes = new ArrayList<>();
//
//        // TODO: use iterator
//        for(Shape shape: shapes) {
//            if (shape.isActive()) {
//                activeShapes.add(shape);
//            }
//            if (shape.hitTest(x, y)) {
//                if (clickedShape == null || clickedShape.getOrder() < shape.getOrder()) {
//                    clickedShape = shape;
//                }
//            }
//        }
//        // set as active
//        if (clickedShape != null && !clickedShape.isActive()) {
//            clickedShape.setActive(true);
//            try {
//                dbStorage.write(clickedShape.getOffset(), clickedShape);
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//
//            // push to back with highest order
//            int maxOrder = shapes.get(shapes.size() - 1).getOrder();
//            clickedShape.setOrder(maxOrder + 1); // TODO: think about overflow
//
//            // TODO: use iterator
//            shapes.remove(clickedShape); // TODO: think about memory usage
//            shapes.add(clickedShape);
//        }
//
//        // reset prev active
//        for (Shape shape: activeShapes) {
//            shape.setActive(false);
//            try {
//                dbStorage.write(shape.getOffset(), shape);
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        }
//        // re-render
//        canvas.repaint();
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

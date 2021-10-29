package lva.spatialindex.viewer.ui;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lva.spatialindex.viewer.repository.ShapeRepository;
import lva.spatialindex.viewer.storage.AbstractShape;
import lva.spatialindex.viewer.storage.Shape;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
public class ShapesViewController {
    private final ShapesViewFrame view;
    private final ShapeRepository shapeRepository;
    private final List<ShapeUI> visibleShapes = new ArrayList<>();

    public ShapesViewController(@NonNull ShapesViewFrame view, @NonNull ShapeRepository shapeRepository) {
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

        Optional<ShapeUI> clickedShape = Lists.reverse(visibleShapes).stream()
                .filter(shape -> shape.hitTest(x, y))
                .findFirst();

        clickedShape.ifPresent(shape -> {
            // push to back with the highest order
            shape.setActive(!shape.isActive());
            shape.setOrder(AbstractShape.Companion.getMaxOrder() + 1);
            AbstractShape.Companion.setMaxOrder(shape.getOrder());

            shapeRepository.update(shape.getUnwrapped());

            visibleShapes.remove(shape);
            visibleShapes.add(shape);

            view.update();
        });
    }

    private void onViewPortChanged() {
        Rectangle viewport = view.getViewport();
        List<ShapeUI> foundShapes = shapeRepository.search(viewport).stream()
                .sorted(Comparator.comparing(Shape::getOrder, Integer::compare))
                .map(ShapesUIKt::asUI)
                .collect(toList());

        visibleShapes.clear();
        visibleShapes.addAll(foundShapes);
        view.setShapes(visibleShapes);

        view.update();
    }

    private void onClosing() {
        shapeRepository.close();
    }
}

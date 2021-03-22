package lva.spatialindex.viewer;

import lva.spatialindex.viewer.controller.ShapeRepositoryController;
import lva.spatialindex.viewer.controller.ShapesViewController;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ProgressFrame;
import lva.spatialindex.viewer.ui.ShapesFrame;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * @author vlitvinenko
 */
public class App {
    private static void buildShapesRepository(Consumer<ShapeRepository> shapeRepositoryConsumer) {
        ShapeRepositoryController controller =
            new ShapeRepositoryController(new ProgressFrame(), shapeRepositoryConsumer);
        controller.build();
    }

    private static void showShapesRepository(ShapeRepository shapeRepository) {
        ShapesViewController controller =
            new ShapesViewController(new ShapesFrame(), shapeRepository);
        controller.run();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            buildShapesRepository((App::showShapesRepository));
        });
    }
}

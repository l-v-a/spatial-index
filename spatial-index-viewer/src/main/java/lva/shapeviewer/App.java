package lva.shapeviewer;

import lva.shapeviewer.controller.ShapeRepositoryController;
import lva.shapeviewer.controller.ShapesViewController;
import lva.shapeviewer.model.ShapeRepository;
import lva.shapeviewer.ui.ProgressFrame;
import lva.shapeviewer.ui.ShapesFrame;

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

package lva.spatialindex.viewer;

import lva.spatialindex.viewer.controller.ShapesViewController;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ShapesFrame;

import javax.swing.SwingUtilities;

import static lva.spatialindex.viewer.repository.ShapeRepositoryKt.buildShapesRepository;


/**
 * @author vlitvinenko
 */
public class App {
    private static void showShapesRepository(ShapeRepository shapeRepository) {
        ShapesViewController controller =
            new ShapesViewController(new ShapesFrame(), shapeRepository);
        controller.run();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String shapesFile = args[0];
            SwingUtilities.invokeLater(() ->
                    buildShapesRepository(shapesFile)
                            .thenAccept(App::showShapesRepository)
            );
        } else {
            System.err.println("shapes file path is required");
        }
    }
}

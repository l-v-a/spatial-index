package lva.spatialindex.viewer;

import lva.spatialindex.viewer.repository.ShapeRepository;
import lva.spatialindex.viewer.ui.ShapesViewController;
import lva.spatialindex.viewer.ui.ShapesViewFrame;

import javax.swing.SwingUtilities;

import static lva.spatialindex.viewer.repository.RepositoryBuilderFrameKt.buildShapesRepository;


/**
 * @author vlitvinenko
 */
public class App {
    private static void showShapesRepository(ShapeRepository shapeRepository) {
        ShapesViewController controller =
            new ShapesViewController(new ShapesViewFrame(), shapeRepository);
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

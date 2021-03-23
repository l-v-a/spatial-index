package lva.spatialindex.viewer;

import lva.spatialindex.viewer.controller.ShapeRepositoryController;
import lva.spatialindex.viewer.controller.ShapesViewController;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ProgressFrame;
import lva.spatialindex.viewer.ui.ShapesFrame;

import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;

/**
 * @author vlitvinenko
 */
public class App {
    private static CompletableFuture<ShapeRepository> buildShapesRepository(String shapesFile) {
        ShapeRepositoryController controller =
                new ShapeRepositoryController(new ProgressFrame(), shapesFile);
        return controller.build();
    }

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

package lva.shapeviewer;

import lva.shapeviewer.controller.ShapesViewController;
import lva.shapeviewer.ui.ProgressFrame;
import lva.shapeviewer.ui.ShapesFrame;

import javax.swing.*;

public class App {
    private static void showShapesRepository(ShapeRepository shapeRepository) {
        ShapesViewController controller = new ShapesViewController(new ShapesFrame(), shapeRepository);
        controller.run();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProgressFrame progressFrame = new ProgressFrame();
            progressFrame.setVisible(true);

            BuildShapeRepositoryWorker buildShapeRepositoryWorker =
                new BuildShapeRepositoryWorker(progressFrame,App::showShapesRepository);

            buildShapeRepositoryWorker.execute();

        });
    }
}

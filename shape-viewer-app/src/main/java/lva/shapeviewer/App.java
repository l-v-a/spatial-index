package lva.shapeviewer;

import lva.shapeviewer.controller.ShapesViewController;
import lva.shapeviewer.ui.ProgressFrame;
import lva.shapeviewer.ui.ShapesFrame;

import javax.swing.*;
import java.util.function.Consumer;

public class App {
    private static void buildShapeRepository(Consumer<ShapeRepository> shapeRepositoryConsumer) {
        ProgressFrame progressFrame = new ProgressFrame();
        progressFrame.setVisible(true);

        BuildShapeRepositoryWorker worker =
            new BuildShapeRepositoryWorker(progressFrame, shapeRepositoryConsumer);

        worker.execute();
    }

    private static void showShapesRepository(ShapeRepository shapeRepository) {
        ShapesViewController controller = new ShapesViewController(new ShapesFrame(), shapeRepository);
        controller.run();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            buildShapeRepository((App::showShapesRepository));
        });
    }
}

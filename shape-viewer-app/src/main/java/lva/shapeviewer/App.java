package lva.shapeviewer;

import lva.shapeviewer.ui.ProgressFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProgressFrame progressFrame = new ProgressFrame();
            progressFrame.setVisible(true);
            BuildShapeRepositoryWorker buildShapeRepositoryWorker = new BuildShapeRepositoryWorker(progressFrame);
            buildShapeRepositoryWorker.execute();
        });
    }
}

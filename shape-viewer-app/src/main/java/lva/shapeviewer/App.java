package lva.shapeviewer;

import lva.shapeviewer.ui.ProgressFrame;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class App {
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeLater(() -> {
            ProgressFrame progressFrame = new ProgressFrame();
            progressFrame.setVisible(true);
            BuildShapeRepositoryWorker buildShapeRepositoryWorker = new BuildShapeRepositoryWorker(progressFrame);
            buildShapeRepositoryWorker.execute();
        });
    }
}

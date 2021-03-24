package lva.spatialindex.viewer.controller;

import lombok.NonNull;
import lombok.SneakyThrows;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ProgressFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * @author vlitvinenko
 */
public class ShapeRepositoryController {
    public static CompletableFuture<ShapeRepository> build(@NonNull ProgressFrame progressView,
                                                           @NonNull String shapesFilePath) {

        CompletableFuture<ShapeRepository> result = new CompletableFuture<>();
        ShapeRepositoryWorker worker = new ShapeRepositoryWorker(Paths.get(shapesFilePath)) {
            @SneakyThrows
            protected void done() {
                progressView.setVisible(false);
                progressView.dispose();
                if (!isCancelled()) {
                    result.complete(get());
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                progressView.setProgress(progress);
            }
        });

        progressView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                worker.cancel(true);
                progressView.setMessage("canceling...");
            }
        });

        progressView.setMessage("indexing...");
        progressView.setVisible(true);
        worker.execute();

        return result;
    }
}

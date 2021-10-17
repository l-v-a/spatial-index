package lva.spatialindex.viewer.controller;

import lombok.NonNull;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ProgressFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNullElse;

/**
 * @author vlitvinenko
 */
public class ShapeRepositoryController {
    public static CompletableFuture<ShapeRepository> buildRepository(@NonNull ProgressFrame progressView,
                                                                     @NonNull String shapesFilePath) {

        CompletableFuture<ShapeRepository> result = new CompletableFuture<>();
        ShapeRepositoryWorker worker = new ShapeRepositoryWorker(Paths.get(shapesFilePath)) {
            protected void done() {
                progressView.dispose();
                if (!isCancelled()) {
                    try {
                        result.complete(get());
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (int) requireNonNullElse(evt.getNewValue(), 0);
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

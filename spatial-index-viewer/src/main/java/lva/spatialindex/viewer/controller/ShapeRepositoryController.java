package lva.spatialindex.viewer.controller;

import lombok.NonNull;
import lombok.SneakyThrows;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.ui.ProgressFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.function.Consumer;

/**
 * @author vlitvinenko
 */
public class ShapeRepositoryController {
    private final ProgressFrame progressView;
    private final ShapeRepositoryWorker worker;
    private final Consumer<ShapeRepository> doneConsumer;

    public ShapeRepositoryController(@NonNull ProgressFrame progressView, @NonNull Consumer<ShapeRepository> doneConsumer) {
        this.progressView = progressView;
        this.doneConsumer = doneConsumer;

        this.worker = new ShapeRepositoryWorker() {
            @Override
            protected void done() {
                handleWorkerDone();
            }
        };

        this.progressView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleProgressViewClosing();
            }
        });

        worker.addPropertyChangeListener(this::propertyChange);
    }

    public void build() {
        progressView.setMessage("indexing...");
        progressView.setVisible(true);
        worker.execute();
    }

    @SneakyThrows
    private void handleWorkerDone() {
        System.out.println("done()");

        progressView.setVisible(false);
        progressView.dispose();

        if (worker.isCancelled()) {
            System.out.println("cancelled");
        } else {
            System.out.println("ok");
            doneConsumer.accept(worker.get());
        }
    }

    private void handleProgressViewClosing() {
        worker.cancel(true);
        progressView.setMessage("canceling...");
    }

    private void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressView.setProgress(progress);
        }
    }
}

package lva.shapeviewer.controller;

import lombok.NonNull;
import lva.shapeviewer.ShapeRepository;
import lva.shapeviewer.ui.ProgressFrame;

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

    private void handleWorkerDone() {
        System.out.println("done()");

        progressView.setVisible(false);
        progressView.dispose();

        if (worker.isCancelled()) {
            System.out.println("cancelled");
        } else {
            try {
                System.out.println("ok");
                doneConsumer.accept(worker.get());
            } catch (Exception e) {
                // TODO: add exception handling / rethrowing
            }
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

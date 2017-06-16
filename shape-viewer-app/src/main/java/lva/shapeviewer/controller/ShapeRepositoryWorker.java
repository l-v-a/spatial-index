package lva.shapeviewer.controller;

import com.google.common.util.concurrent.MoreExecutors;
import lva.shapeviewer.model.MultiIndex;
import lva.shapeviewer.model.Shape;
import lva.shapeviewer.model.ShapeRepository;
import lva.shapeviewer.model.ShapeStorage;
import lva.shapeviewer.utils.AutoCloseables;
import lva.spatialindex.Storage;
import lva.spatialindex.index.Index;

import javax.swing.*;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

import static java.util.Arrays.asList;

/**
 * @author vlitvinenko
 */
class ShapeRepositoryWorker extends SwingWorker<ShapeRepository, Void> {
    private static final int MAX_ELEMENTS_IN_TREE = 1000 * 1000;

    ShapeRepositoryWorker() {}

    @Override
    protected ShapeRepository doInBackground() throws Exception {
        Storage<Shape> shapeStorage = null;
        Index index = null;

        try {
            shapeStorage = new ShapeStorage("/home/vlitvinenko/work/lab/rtree/db.bin", 64 * 1024L * 1024L);
            index = new MultiIndex(buildIndexes(shapeStorage));
            System.out.printf("build ok\n");

            return new ShapeRepository(shapeStorage, index);
        } catch (Exception e) {
            close(asList(shapeStorage, index));
            throw e;
        }
    }

    private Collection<Index> buildIndexes(Storage<Shape> shapeStorage) throws Exception {
        Collection<Index> indexes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            int maxRows = 100;
            int maxCols = 100;
            int numOfElements = maxRows * maxCols;
            int numOfTasks = (numOfElements + MAX_ELEMENTS_IN_TREE - 1) / MAX_ELEMENTS_IN_TREE;

            BlockingQueue<BuildIndexTask.IndexData> objectsQueue = new LinkedBlockingQueue<>(1000 * 1000);
            Collection<Future<Index>> futures = new ArrayList<>(numOfTasks);

            for (int taskNumber = 0; taskNumber < numOfTasks; taskNumber++) {
                futures.add(executor.submit(new BuildIndexTask(objectsQueue, taskNumber, MAX_ELEMENTS_IN_TREE)));
            }

            executor.shutdown();

            System.out.println("start build");
            Instant start = Instant.now();

            int off = 0;
            int count = 0;
            int percent = -1;

            for (int row = 0; row < maxRows; row++) {
                for (int col = 0; col < maxCols; col++) {
                    Rectangle r = new Rectangle(off + 3 + row * 35, off + 3 + col * 35, 50, 50);
                    // Rectangle r = new Rectangle(3 + row * 15 + col % 50 , 3 + col * 15 + row % 50, 10 + col % 50, 10 + row % 50);
                    Shape shape = new Shape(r);
                    shape.setOrder(count);
                    long offset = shapeStorage.add(shape);

                    objectsQueue.put(new BuildIndexTask.IndexData(offset, shape.getMbr()));
                    count++;
                    int newPercent = (int) ((count * 100L) / numOfElements);
                    if (newPercent != percent) {
                        percent = newPercent;
                        System.out.printf("\rputted %d%%", percent);
                        setProgress(Math.min(percent,100)); // TODO: simplify
                    }

                }
            }

            Rectangle r = new Rectangle(5000 - 30, 5000 - 30, 30, 30);
            Shape shape = new Shape(r);
            long offset = shapeStorage.add(shape);

            objectsQueue.put(new BuildIndexTask.IndexData(offset, shape.getMbr()));
            objectsQueue.put(BuildIndexTask.NULL_INDEX_DATA); // TODO: better to interrupt all tasks? ...
            // or (Future<Result> f : futures)
            // f.cancel(true);
            // TODO: think about to use CompletionService

            for (Future<Index> f : futures) {
                indexes.add(f.get());
            }

            Duration d = Duration.between(start, Instant.now());
            System.out.printf("\nBuild in time %d ms%n", d.toMillis());

        } catch (Exception exc) {
            MoreExecutors.shutdownAndAwaitTermination(executor, 1, TimeUnit.SECONDS);
            AutoCloseables.close(indexes, exc);
            // TODO: remove files
        }

        return indexes;
    }
}

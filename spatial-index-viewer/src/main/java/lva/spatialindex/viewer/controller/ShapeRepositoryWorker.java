package lva.spatialindex.viewer.controller;

import com.google.common.util.concurrent.MoreExecutors;
import lva.spatialindex.index.Index;
import lva.spatialindex.storage.Storage;
import lva.spatialindex.utils.Exceptions;
import lva.spatialindex.viewer.index.MultiIndex;
import lva.spatialindex.viewer.model.ShapeRepository;
import lva.spatialindex.viewer.storage.Shape;
import lva.spatialindex.viewer.storage.ShapeStorage;
import lva.spatialindex.viewer.utils.AutoCloseables;

import javax.swing.*;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
class ShapeRepositoryWorker extends SwingWorker<ShapeRepository, Void> {
    private static final int MAX_ELEMENTS_IN_TREE = 1000 * 1000;
    private static final long STORAGE_INITIAL_SIZE = 64 * 1024L * 1024L;
    private static final int SHAPES_QUEUE_CAPACITY = 1000 * 1000;
    private static final int SIZE_OF_SHAPE_BYTES_AVG = 26;
    private static final String DB_FILE_NAME = "shapes.bin";

    private final Path shapesFile;

    ShapeRepositoryWorker(Path shapesFile) {
        this.shapesFile = shapesFile;
    }

    @Override
    protected ShapeRepository doInBackground() throws Exception {
        Storage<Shape> shapeStorage = null;
        Index index = null;

        try {
            shapeStorage = new ShapeStorage(shapesFile.resolveSibling(DB_FILE_NAME).toString(), STORAGE_INITIAL_SIZE);
            index = new MultiIndex(buildIndexes(shapeStorage));
            return new ShapeRepository(shapeStorage, index);
        } catch (Exception e) {
            AutoCloseables.close(asList(shapeStorage, index));
            throw e;
        }
    }

    private Collection<Index> buildIndexes(Storage<Shape> shapeStorage) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            Path indexPath = shapesFile.getParent();
            int numberOfShapesEstimated = Math.max((int) Files.size(shapesFile) / SIZE_OF_SHAPE_BYTES_AVG, 100);
            int numOfTasks = (numberOfShapesEstimated + MAX_ELEMENTS_IN_TREE - 1) / MAX_ELEMENTS_IN_TREE;

            BlockingQueue<BuildIndexTask.IndexData> shapesQueue = new LinkedBlockingQueue<>(SHAPES_QUEUE_CAPACITY);

            System.out.printf("Starting. shapes number est.: %s\n tasks number: %s\n",
                numberOfShapesEstimated, numOfTasks);

            Collection<CompletableFuture<Index>> indexingTasks = IntStream.range(0, numOfTasks)
                    .mapToObj((taskNumber) -> new BuildIndexTask(shapesQueue, indexPath, taskNumber, MAX_ELEMENTS_IN_TREE))
                    .map((task) -> CompletableFuture.supplyAsync(task::call, executor))
                    .collect(toList());

            try (BufferedReader reader = Files.newBufferedReader(shapesFile)) {
                setProgress(0);

                Stream<Shape> shapesStream = reader.lines().filter((line) -> !line.trim().isEmpty())
                        .map((line) -> {
                            Shape shape = ShapeParser.parseShape(line);
                            shape.setMaxOrder(shape.getMaxOrder() + 1);
                            shape.setOrder(shape.getMaxOrder());
                            return shape;
                        });

                shapesStream.forEachOrdered((shape) -> {
                    Exceptions.toRuntime(() -> {
                        // add to storage
                        long offset = shapeStorage.add(shape);

                        // scatter to tasks for indexing
                        shapesQueue.put(new BuildIndexTask.IndexData(offset, shape.getMbr()));

                        int percent = (int) ((shape.getOrder() * 100L) / numberOfShapesEstimated);
                        setProgress(Math.min(percent, 100));
                    });
                });

            }

            // last task marker
            shapesQueue.put(BuildIndexTask.NULL_INDEX_DATA);

            System.out.println("All shapes added, waiting for tasks");

            Collection<Index> indexes = indexingTasks.stream().map(CompletableFuture::join).collect(toList());
            setProgress(100);

            return indexes;

        } finally {
            MoreExecutors.shutdownAndAwaitTermination(executor, 5, TimeUnit.SECONDS);
        }
    }
}


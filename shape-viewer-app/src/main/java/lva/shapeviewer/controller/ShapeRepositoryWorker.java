package lva.shapeviewer.controller;

import com.google.common.util.concurrent.MoreExecutors;
import lva.shapeviewer.index.MultiIndex;
import lva.shapeviewer.model.ShapeRepository;
import lva.shapeviewer.storage.CircleShape;
import lva.shapeviewer.storage.RectangleShape;
import lva.shapeviewer.storage.Shape;
import lva.shapeviewer.storage.ShapeStorage;
import lva.shapeviewer.utils.AutoCloseables;
import lva.spatialindex.index.Index;
import lva.spatialindex.storage.Storage;

import javax.swing.*;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            AutoCloseables.close(asList(shapeStorage, index));
            throw e;
        }
    }

    private static final int SIZE_OF_SHAPE_BYTES_AVG = 26;
    private Collection<Index> buildIndexes(Storage<Shape> shapeStorage) throws Exception {
        Collection<Index> indexes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {

            // TODO: add file existence special handling
            Path shapesFile = Paths.get("/home/vlitvinenko/work/lab/rtree/shapes.txt");
            int numberOfShapesEstimated = Math.max((int) Files.size(shapesFile) / SIZE_OF_SHAPE_BYTES_AVG, 100);
            int numOfTasks = (numberOfShapesEstimated + MAX_ELEMENTS_IN_TREE - 1) / MAX_ELEMENTS_IN_TREE;

            BlockingQueue<BuildIndexTask.IndexData> objectsQueue = new LinkedBlockingQueue<>(1000 * 1000);
            Collection<Future<Index>> futures = new ArrayList<>(numOfTasks);

            System.out.printf("Starting. shapes number est.: %s\n tasks number: %s\n",
                numberOfShapesEstimated, numOfTasks);

            for (int taskNumber = 0; taskNumber < numOfTasks; taskNumber++) {
                futures.add(executor.submit(new BuildIndexTask(objectsQueue, taskNumber, MAX_ELEMENTS_IN_TREE)));
            }

            executor.shutdown();

            try (BufferedReader reader = Files.newBufferedReader(shapesFile)) {
                int order = 0;
                String line;

                setProgress(0);

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        // parse to shape
                        Shape shape = ShapeParser.parseShape(line);
                        shape.setOrder(order++);
                        shape.setMaxOrder(shape.getOrder());

                        // add to storage
                        long offset = shapeStorage.add(shape);

                        // scatter to tasks for indexing
                        objectsQueue.put(new BuildIndexTask.IndexData(offset, shape.getMbr()));

                        int percent = (int) ((order * 100L) / numberOfShapesEstimated);
                        setProgress(Math.min(percent, 100));
                    }
                }
            }

            // last task marker
            objectsQueue.put(BuildIndexTask.NULL_INDEX_DATA);

            System.out.println("All shapes added, waiting for tasks");

            for (Future<Index> f : futures) {
                indexes.add(f.get());
            }

            setProgress(100);

        } catch (Exception exc) {
            MoreExecutors.shutdownAndAwaitTermination(executor, 1, TimeUnit.SECONDS);
            AutoCloseables.close(indexes, exc);
            // TODO: remove files
        }

        return indexes;
    }

    private static class ShapeParser {
        private static final Pattern SHAPE_FORMAT_PATTERN = Pattern.compile("(\\w+):(.+)");
        private static Shape parseShape(String str) {
            Matcher matcher= SHAPE_FORMAT_PATTERN.matcher(str);
            if (matcher.matches()) {
                try {
                    String type = matcher.group(1).trim().toLowerCase();
                    String params = matcher.group(2).trim();
                    List<Integer> args = Arrays.stream(params.split("\\s*,\\s*"))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList());

                    switch (type) {
                        case "rect": {
                            int x = args.get(0);
                            int y = args.get(1);
                            int w = args.get(2);
                            int h = args.get(3);
                            return new RectangleShape(x, y, w, h);
                        }

                        case "circle": {
                            int x = args.get(0);
                            int y = args.get(1);
                            int r = args.get(2);
                            return new CircleShape(x, y, r);
                        }

                        default:
                            throw new IllegalArgumentException("Unknown shape type");
                    }

                } catch (Exception exc) {
                    throw new IllegalArgumentException(String.format("Bad shape input string: %s", str), exc);
                }
            }

            throw new IllegalArgumentException(String.format("Bad shape input string: %s", str));
        }
    }
}

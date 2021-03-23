package lva.spatialindex.viewer.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lva.spatialindex.index.Index;
import lva.spatialindex.index.RStarTree;
import lva.spatialindex.utils.Exceptions;
import lva.spatialindex.viewer.utils.AutoCloseables;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.Collections.singleton;

/**
 * @author vlitvinenko
 */
class BuildIndexTask implements Callable<Index> {
    private static final String INDEX_FILE_NAME_FORMAT = "shapes_idx%d.bin";

    @RequiredArgsConstructor
    static class IndexData {
        private final long offset;
        private final Rectangle mbr;
        static IndexData of(long offset, Rectangle mbr) {
            return new IndexData(offset, mbr);
        }
    }

    static final IndexData NULL_INDEX_DATA = new IndexData(0, new Rectangle());

    private final int maxNumberOfElements;
    private final BlockingQueue<IndexData> objectsQueue;
    private final int taskNumber;
    private final Path indexPath;

    BuildIndexTask(@NonNull BlockingQueue<IndexData> objectsQueue,
                   @NonNull Path indexPath,
                   int taskNumber,
                   int maxNumberOfElements) {

        this.maxNumberOfElements = maxNumberOfElements;
        this.indexPath = indexPath;
        this.objectsQueue = objectsQueue;
        this.taskNumber = taskNumber;
    }

    @Override
    public Index call() {
        return Exceptions.toRuntime(() -> {
            Path storageFile = indexPath.resolve(format(INDEX_FILE_NAME_FORMAT, taskNumber));
            RStarTree indexTree = new RStarTree(maxNumberOfElements, storageFile.toString());

            try {
                for (int count = 0; count < maxNumberOfElements; count++) {
                    IndexData indexData = objectsQueue.take();
                    if (indexData == NULL_INDEX_DATA) {
                        objectsQueue.put(NULL_INDEX_DATA);
                        break;
                    }
                    indexTree.insert(indexData.offset, indexData.mbr);
                }

                return indexTree;

            } catch (Exception exc) {
                System.out.printf("task %s closed by exception\n", taskNumber);
                AutoCloseables.close(singleton(indexTree));
                Files.deleteIfExists(storageFile);
                throw exc;
            }
        });
    }

    CompletableFuture<Index> callAsync(ExecutorService executorService) {
        return CompletableFuture.supplyAsync(this::call, executorService);
    }

}

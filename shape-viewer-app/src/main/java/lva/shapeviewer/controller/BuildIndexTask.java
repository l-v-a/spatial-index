package lva.shapeviewer.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lva.shapeviewer.utils.AutoCloseables;
import lva.shapeviewer.utils.Settings;
import lva.spatialindex.index.Index;
import lva.spatialindex.index.RStarTree;

import java.awt.*;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

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
    }

    static final IndexData NULL_INDEX_DATA = new IndexData(0, new Rectangle());

    private final int maxNumberOfElements;
    private final BlockingQueue<IndexData> objectsQueue;
    private final int taskNumber;

    BuildIndexTask(@NonNull BlockingQueue<IndexData> objectsQueue, int taskNumber, int maxNumberOfElements) {
        this.maxNumberOfElements = maxNumberOfElements;
        this.objectsQueue = objectsQueue;
        this.taskNumber = taskNumber;
    }

    @Override
    public Index call() throws Exception {
        String storageFile = Paths.get(Settings.getDbPath().toString(), String.format(INDEX_FILE_NAME_FORMAT, taskNumber)).toString();
        RStarTree indexTree = new RStarTree(maxNumberOfElements, storageFile);

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
            AutoCloseables.close(singleton(indexTree), exc);
            throw exc;
        }
    }
}

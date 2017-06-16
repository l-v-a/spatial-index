package lva.shapeviewer.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lva.shapeviewer.utils.AutoCloseables;
import lva.spatialindex.index.Index;
import lva.spatialindex.index.RStarTree;

import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import static java.util.Collections.singleton;

/**
 * @author vlitvinenko
 */
class BuildIndexTask implements Callable<Index> {
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
        RStarTree indexTree = new RStarTree(maxNumberOfElements, "/home/vlitvinenko/work/lab/rtree/buff.bin" + taskNumber);

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

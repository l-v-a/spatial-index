package lva.shapeviewer;

import lva.spatialindex.index.Index;
import lva.spatialindex.index.RStarTree;

import java.awt.Rectangle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import static java.util.Collections.singleton;
import static lva.shapeviewer.AutoCloseables.close;

/**
 * @author vlitvinenko
 */
public class BuildIndexTask implements Callable<Index> {
    static class IndexData {
        final long offset;
        final Rectangle mbr;

        IndexData(long offset, Rectangle mbr) {
            this.offset = offset;
            this.mbr = mbr;
        }
    }

    static final IndexData NULL_INDEX_DATA = new IndexData(0, null);

    private final int numOfElements;
    private final BlockingQueue<IndexData> objectsQueue;
    private final int taskNumber;

    public BuildIndexTask(int numOfElements, BlockingQueue<IndexData> objectsQueue, int taskNumber) {
        this.numOfElements = numOfElements;
        this.objectsQueue = objectsQueue;
        this.taskNumber = taskNumber;
    }

    @Override
    public Index call() throws Exception {
        RStarTree indexTree = new RStarTree(numOfElements, "/home/vlitvinenko/work/lab/rtree/buff.bin" + taskNumber);

        try {
            for (int count = 0; count < numOfElements; count++) {
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
            close(singleton(indexTree), exc);
            throw exc;
        }
    }
}

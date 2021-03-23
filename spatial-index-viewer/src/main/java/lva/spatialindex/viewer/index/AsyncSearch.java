package lva.spatialindex.viewer.index;

import lva.spatialindex.index.Index;
import lva.spatialindex.viewer.utils.ExecutorUtils;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author vlitvinenko
 */
public interface AsyncSearch {

    CompletableFuture<Collection<Long>> byIndex(Index index);

    static AsyncSearch of(Rectangle area) {
        return of(area, ExecutorUtils.EXECUTOR_SERVICE);
    }

    static AsyncSearch of(Rectangle area, ExecutorService executorService) {
        return index -> CompletableFuture.supplyAsync(() -> index.search(area), executorService);
    }
}

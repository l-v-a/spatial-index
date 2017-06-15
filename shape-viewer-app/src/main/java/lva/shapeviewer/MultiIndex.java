package lva.shapeviewer;

import lombok.SneakyThrows;
import lva.spatialindex.Exceptions;
import lva.spatialindex.index.Index;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author vlitvinenko
 */
public class MultiIndex implements Index {
    private final Collection<Index> indexes;

    public MultiIndex(Collection<Index> indexes) {
        this.indexes = indexes;
    }

    @Override
    @SneakyThrows
    public Collection<Long> search(Rectangle area) {
        List<Future<Collection<Long>>> searchResults = new ArrayList<>(indexes.size());
        for (Index index : indexes) {
            searchResults.add(ExecutorUtils.EXECUTOR_SERVICE.submit(() -> index.search(area)));
        }

        Collection<Long> result = new ArrayList<>();
        for (Future<Collection<Long>> searchResult : searchResults) {
            result.addAll(searchResult.get());
        }

        return result;

    }

    @Override
    public void close() {
        Exceptions.toRuntime(() -> AutoCloseables.close(indexes));
    }
}

package lva.shapeviewer.index;

import lombok.NonNull;
import lva.shapeviewer.utils.AutoCloseables;
import lva.shapeviewer.utils.ExecutorUtils;
import lva.spatialindex.index.Index;
import lva.spatialindex.utils.Exceptions;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

/**
 * @author vlitvinenko
 */
public class MultiIndex implements Index {
    private final Collection<Index> indexes;

    public MultiIndex(@NonNull Collection<Index> indexes) {
        this.indexes = indexes;
    }

    @Override
    @NonNull
    public Collection<Long> search(@NonNull Rectangle area) {
        List<CompletableFuture<Collection<Long>>> searchTasks =
            indexes.stream().map((index) -> CompletableFuture.supplyAsync(() -> index.search(area), ExecutorUtils.EXECUTOR_SERVICE))
                    .collect(toList());

        return searchTasks.stream().flatMap(f -> f.join().stream()).collect(toList());

    }

    @Override
    public void close() {
        Exceptions.toRuntime(() -> AutoCloseables.close(indexes));
    }
}

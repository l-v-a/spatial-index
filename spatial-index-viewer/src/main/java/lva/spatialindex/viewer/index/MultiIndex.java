package lva.spatialindex.viewer.index;

import lombok.NonNull;
import lva.spatialindex.index.Index;
import lva.spatialindex.utils.Exceptions;
import lva.spatialindex.viewer.utils.AutoCloseables;

import java.awt.Rectangle;
import java.util.Collection;
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
        AsyncSearch search = AsyncSearch.of(area);
        var results = indexes.stream().map(search::byIndex).collect(toList());

        return results.stream().map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    @Override
    public void close() {
        Exceptions.toRuntime(() -> AutoCloseables.close(indexes));
    }
}

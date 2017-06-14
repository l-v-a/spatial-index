package lva.spatialindex.index;

import java.awt.Rectangle;
import java.util.Collection;

/**
 * @author vlitvinenko
 */
public interface Index extends AutoCloseable {
    Collection<Long> search(Rectangle area);
    void close();
}

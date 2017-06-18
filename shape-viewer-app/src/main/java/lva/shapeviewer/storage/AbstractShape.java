package lva.shapeviewer.storage;

import lombok.Getter;
import lombok.Setter;

/**
 * @author vlitvinenko
 */
abstract class AbstractShape implements Shape {
    @Getter
    @Setter
    protected boolean isActive;

    @Getter
    @Setter
    protected int order;

    @Getter
    @Setter
    protected long offset;
}

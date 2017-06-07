package lva.spatialindex.index;

import java.awt.*;

/**
 * @author vlitvinenko
 */
class Rectangles {
    private Rectangles() {}

    static long area(Rectangle r) {
        return r.width * r.height;
    }

    static int margin(Rectangle r) {
        return 2 * (r.height + r.width);
    }
}

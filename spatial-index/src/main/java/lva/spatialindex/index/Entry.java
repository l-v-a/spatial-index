package lva.spatialindex.index;

import lva.spatialindex.Storage;

import java.awt.*;
import java.util.Comparator;

/**
 * @author vlitvinenko
 */
class Entry {
    private static final int SIZE_OF_X = 4;
    private static final int SIZE_OF_Y = 4;
    private static final int SIZE_OF_WIDTH = 4;
    private static final int SIZE_OF_HEIGHT = 4;
    private static final int SIZE_OF_CHILD = 8;
    static final int SIZE = SIZE_OF_X + SIZE_OF_Y + SIZE_OF_WIDTH + SIZE_OF_HEIGHT + SIZE_OF_CHILD;

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_LEFT_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.x, e2.mbr.x);
    };

    static final Comparator<Entry> TOP_TO_BOTTOM_BOTTOM_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.y + e1.mbr.height, e2.mbr.y + e2.mbr.height);
    };

    static final Comparator<Entry> TOP_TO_BOTTOM_TOP_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.y, e2.mbr.y);
    };

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.x + e1.mbr.width, e2.mbr.x + e2.mbr.width);
    };


    private final Storage<Node> storage;
    final long childOffset;
    Rectangle mbr; // TODO: think about to make final


    Entry(Storage<Node> storage, Rectangle mbr, long childOffset) {
        this.storage = storage;
        this.mbr = mbr;
        this.childOffset = childOffset;
    }

    Node loadNode() {
        return childOffset >= 0 ? storage.get(childOffset) : null;
    }

    long getChildOffset() {
        return childOffset;
    }

    static Entry of(Storage<Node> storage, Rectangle mbr, long nodeOffset) {
        return new Entry(storage, mbr, nodeOffset);
    }
}

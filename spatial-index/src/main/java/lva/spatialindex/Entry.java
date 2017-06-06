package lva.spatialindex;

import java.awt.*;

/**
 * @author vlitvinenko
 */
class Entry {
    private static int SIZE_OF_X = 4;
    private static int SIZE_OF_Y = 4;
    private static int SIZE_OF_WIDTH = 4;
    private static int SIZE_OF_HEIGHT = 4;
    private static int SIZE_OF_CHILD = 8;

    static int SIZE = SIZE_OF_X + SIZE_OF_Y + SIZE_OF_WIDTH + SIZE_OF_HEIGHT + SIZE_OF_CHILD;

    private final NodeStorage storage;
    final long childOffset;
    Rectangle mbr; // TODO: think about to make final

    Entry(NodeStorage storage, Rectangle mbr, long childOffset) {
        this.storage = storage;
        this.mbr = mbr;
        this.childOffset = childOffset;
    }

    public Node loadNode() throws Exception {
        return childOffset >= 0 ? storage.get(childOffset) : null;
    }


    @Override
    public String toString() {
        return mbr.toString() + ", mbr arra: " + (mbr.width * mbr.height) + ", offsetAbsolute: " + String.valueOf(childOffset);
    }

    long getChildOffset() {
        return childOffset;
    }

    static Entry of(NodeStorage storage, Rectangle mbr, long nodeOffset) {
        return new Entry(storage, mbr, nodeOffset);
    }
}

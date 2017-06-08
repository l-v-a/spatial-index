package lva.spatialindex.index;

import lombok.EqualsAndHashCode;
import lva.spatialindex.Storage;

import java.awt.Rectangle;
import java.util.Comparator;
import java.util.List;


/**
 * @author vlitvinenko
 */
@EqualsAndHashCode(exclude = {"storage"})
class Entry {
    private static final int SIZE_OF_X = 4;
    private static final int SIZE_OF_Y = 4;
    private static final int SIZE_OF_WIDTH = 4;
    private static final int SIZE_OF_HEIGHT = 4;
    private static final int SIZE_OF_CHILD = 8;
    static final int SIZE = SIZE_OF_X + SIZE_OF_Y + SIZE_OF_WIDTH + SIZE_OF_HEIGHT + SIZE_OF_CHILD;

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_LEFT_COMPARATOR = (e1, e2) ->
        Integer.compare(e1.mbr.x, e2.mbr.x);

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR = (e1, e2) ->
        Integer.compare(e1.mbr.x + e1.mbr.width, e2.mbr.x + e2.mbr.width);

    static final Comparator<Entry> TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR = (e1, e2) ->
        Integer.compare(e1.mbr.y + e1.mbr.height, e2.mbr.y + e2.mbr.height);

    static final Comparator<Entry> TOP_TO_BOTTOM_BY_TOP_COMPARATOR = (e1, e2) ->
        Integer.compare(e1.mbr.y, e2.mbr.y);


    private final Storage<Node> storage;
    private final long childOffset;
    private Rectangle mbr; // TODO: think about to make final


    Entry(Storage<Node> storage, Rectangle mbr, long childOffset) {
        this.storage = storage;
        this.mbr = mbr;
        this.childOffset = childOffset;
    }

    Node getChildNode() {
        return childOffset >= 0 ? storage.get(childOffset) : null;
    }

    long getChildOffset() {
        return childOffset;
    }

    Rectangle getMbr() {
        return mbr; // TODO: think about defensive copy
    }

    void setMbr(Rectangle mbr) {
        this.mbr = mbr;
    }

    boolean isLeaf() {
        return childOffset < 0;
    }

    static Rectangle union(List<Entry> entries) { // TODO: use Collection or stream
        Rectangle r = entries.isEmpty() ? new Rectangle() : entries.get(0).mbr;
        for (Entry e: entries) {
            r = r.union(e.mbr);
        }
        return r;
    }

    static int margin(List<Entry> entries) {
        int margin = 0;
        for (Entry e: entries) {
            margin += Rectangles.margin(e.mbr);
        }
        return margin;
    }

}

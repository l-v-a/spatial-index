package lva.spatialindex.index;

import lombok.EqualsAndHashCode;
import lva.spatialindex.storage.Storage;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


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

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_LEFT_COMPARATOR =
            Comparator.comparingInt(e -> e.mbr.x);

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR =
            Comparator.comparingInt(e -> e.mbr.x + e.mbr.width);

    static final Comparator<Entry> TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR =
            Comparator.comparingInt(e -> e.mbr.y + e.mbr.height);

    static final Comparator<Entry> TOP_TO_BOTTOM_BY_TOP_COMPARATOR =
            Comparator.comparingInt(e -> e.mbr.y);

    private final Storage<Node> storage;
    private final long childOffset;
    private Rectangle mbr; // TODO: think about to make final


    Entry(Storage<Node> storage, Rectangle mbr, long childOffset) {
        this.storage = storage;
        this.mbr = mbr;
        this.childOffset = childOffset;
    }

    // TODO: use Either
    Optional<Node> getChildNode() {
        return Optional.ofNullable(childOffset >= 0 ? storage.read(childOffset) : null);
    }

    long getChildOffset() {
        return childOffset;
    }

    Rectangle getMbr() {
        return mbr;
    }

    void setMbr(Rectangle mbr) {
        this.mbr = mbr;
    }

    boolean isLeaf() {
        return childOffset < 0;
    }

    static Rectangle union(List<Entry> entries) {
        return entries.stream()
                .map(Entry::getMbr).reduce(Rectangle::union)
                .orElse(new Rectangle());
    }

    static int margin(List<Entry> entries) {
        return entries.stream()
                .mapToInt(e -> Rectangles.margin(e.mbr)).sum();
    }
}

package lva.spatialindex.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.EqualsAndHashCode;
import lva.spatialindex.storage.Storage;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * @author vlitvinenko
 */
@EqualsAndHashCode(exclude = {"storage"})
public
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

    static final Collection<Comparator<Entry>> X_COMPARATORS =
            Arrays.asList(LEFT_TO_RIGHT_BY_LEFT_COMPARATOR, LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR);

    static final Collection<Comparator<Entry>> Y_COMPARATORS =
            Arrays.asList(TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR, TOP_TO_BOTTOM_BY_TOP_COMPARATOR);

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
                .mapToInt(e -> Rectangles.margin(e.mbr))
                .sum();
    }

    static class Ser extends Serializer<Entry> {
        private final Storage<Node> storage;
        Ser(Storage<Node> storage) {
            this.storage = storage;
        }

        @Override
        public void write(Kryo kryo, Output output, Entry entry) {
            Rectangle mbr = entry.getMbr();

            output.writeInt(mbr.x);
            output.writeInt(mbr.y);
            output.writeInt(mbr.width);
            output.writeInt(mbr.height);
            output.writeLong(entry.getChildOffset());
        }

        @Override
        public Entry read(Kryo kryo, Input input, Class<Entry> type) {
            int x = input.readInt();
            int y = input.readInt();
            int width = input.readInt();
            int height = input.readInt();
            long childOffset = input.readLong();

            return new Entry(storage, new Rectangle(x, y, width, height), childOffset);
        }
    }
}

package lva.spatialindex.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.EqualsAndHashCode;
import lva.spatialindex.storage.Storage;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static lva.spatialindex.index.Entry.union;


/**
 * @author vlitvinenko
 */
@EqualsAndHashCode(exclude = {"storage", "mbr"})
class Node {
    static final int PAGE_SIZE = 4096;
    static final int MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1;
    static final int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

    private final Storage<Node> storage;
    private final List<Entry> entries = new ArrayList<>();
    private long offset;
    private long parentOffset = -1;
    private Rectangle mbr = null;

    Node(Storage<Node> storage, long offset) {
        this.storage = storage;
        this.offset = offset;
    }

    static Node newNode(Storage<Node> storage) {
        Node node = new Node(storage, -1);
        storage.add(node);
        return node;
    }

    boolean isLeaf() {
        return entries.stream().findAny().map(Entry::isLeaf)
                .orElse(true);
    }

    boolean isFull() {
        return entries.size() >= MAX_ENTRIES;
    }

    Rectangle getMbr() {
        if (mbr == null) {
            mbr = union(entries);
        }
        return mbr;
    }

    Node resetMbr() {
        mbr = null;
        return this;
    }

    Node setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    long getOffset() {
        return offset;
    }

    long getParentOffset() {
        return parentOffset;
    }

    List<Entry> getEntries() {
        return entries;
    }

    Node addNode(Node node) {
        return addEntry(new Entry(storage, node.getMbr(), node.getOffset()));
    }

    Node addEntry(Entry entry) {
        return putEntry(entry).save();
    }

    Node setEntries(List<Entry> entries) {
        this.entries.clear();
        entries.forEach(this::putEntry);
        return save();
    }

    private Node putEntry(Entry entry) {
        checkState(!isFull(), "Entries overflow");

        entries.add(entry);
        resetMbr();

        entry.getChildNode().ifPresent(childNode -> {
            childNode.parentOffset = offset;
            childNode.save();
        });

        return this;
    }

    Node save() {
        storage.write(offset, this);
        return this;
    }

    static class Ser extends Serializer<Node> {
        private final Storage<Node> storage;
        Ser(Storage<Node> storage) {
            this.storage = storage;
        }

        @Override
        public void write(Kryo kryo, Output output, Node node) {
            output.writeLong(node.parentOffset);

            output.writeInt(node.entries.size());
            for (Entry entry : node.entries) {
                kryo.writeObject(output, entry);
            }
        }

        @Override
        public Node read(Kryo kryo, Input input, Class<Node> type) {
            Node node = new Node(storage, -1);
            node.parentOffset = input.readLong();

            int entriesSize = input.readInt();
            while (entriesSize > 0) {
                node.entries.add(kryo.readObject(input, Entry.class));
                entriesSize--;
            }
            return node;
        }
    }
}

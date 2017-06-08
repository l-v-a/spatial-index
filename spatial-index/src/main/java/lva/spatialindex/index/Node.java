package lva.spatialindex.index;

import lombok.EqualsAndHashCode;
import lva.spatialindex.Exceptions;
import lva.spatialindex.Storage;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import static lva.spatialindex.index.Entry.union;


/**
 * @author vlitvinenko
 */
@EqualsAndHashCode(exclude = {"storage", "mbr"})
class Node {
    static final int PAGE_SIZE = 4096; // TODO: calculate
    static final int MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1;
    static final int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

    private final Storage<Node> storage;
    private long offset;
    private long parentOffset = -1;

    private Rectangle mbr = null;
    private final List<Entry> entries = new ArrayList<>(); // TODO: test perf. with ArrayList<>(MAX_ENTRIES)

    private Node(Storage<Node> storage, long offset) {
        this.storage = storage; // TODO: rename to buffer
        this.offset = offset;
    }

    static Node newNode(Storage<Node> storage) {
        Node node = new Node(storage, -1);
        storage.add(node);
        return node;
    }

    Node save() {
        storage.write(offset, this);
        return this;
    }

    byte[] serialize() {
        return Exceptions.runtime(() -> {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(baos);

            os.writeLong(parentOffset);
            os.writeInt(entries.size());

            for (Entry entry: entries) {
                Rectangle mbr = entry.getMbr();

                os.writeInt(mbr.x);
                os.writeInt(mbr.y);
                os.writeInt(mbr.width);
                os.writeInt(mbr.height);
                os.writeLong(entry.getChildOffset());
            }

            return baos.toByteArray();
        });
    }


    Node deserialize(byte[] buff) {
        return Exceptions.runtime(() -> {

            ByteArrayInputStream bais = new ByteArrayInputStream(buff);
            DataInputStream is = new DataInputStream(bais);

            parentOffset = is.readLong();

            int entriesSize = is.readInt();
            entries.clear();

            for (int i = 0; i < entriesSize; i++) {
                int x = is.readInt();
                int y = is.readInt();
                int width = is.readInt();
                int height = is.readInt();
                long childOffset = is.readLong();
                entries.add(new Entry(storage, new Rectangle(x, y, width, height), childOffset));
            }

            return this;
        });
    }

    boolean isLeaf() {
        return entries.isEmpty() || entries.get(0).isLeaf();
    }

    boolean isFull() {
        return entries.size() >= MAX_ENTRIES;
    }

    Node addNode(Node node) {
        if (isFull()) {
            throw new IllegalStateException("entries overflow");
        }

        entries.add(new Entry(storage, node.getMbr(), node.getOffset()));

        node.parentOffset = offset;
        resetMbr();

        node.save();
        save();

        return this;
    }

    Rectangle getMbr() {
        if (mbr == null) {
            mbr = union(entries);
        }
        return mbr; // TODO: add defensive copy
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
        return entries; // TODO: add defensive copy
    }

    Node addEntry(Entry entry) {
        putEntry(entry);
        save();
        return this;
    }

    Node setEntries(List<Entry> entries) {
        this.entries.clear();
        for (Entry e: entries) {
            putEntry(e);
        }

        resetMbr();
        save();
        return this;
    }

    private Node putEntry(Entry entry) {
        // TODO: think about to refactor this addNode(Node node)
        if (isFull()) {
            throw new IllegalStateException("entries overflow");
        }

        entries.add(entry);
        resetMbr();

        Node node = entry.loadNode();
        if (node != null) {
            node.parentOffset = offset;
            node.save();
        }

        return this;
    }
}

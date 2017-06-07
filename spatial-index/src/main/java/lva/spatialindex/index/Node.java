package lva.spatialindex.index;

import lva.spatialindex.Exceptions;
import lva.spatialindex.Storage;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @author vlitvinenko
 */
class Node {
    private final Storage<Node> storage;
    private long offset;
    private long parentOffset = -1;

    private Rectangle mbr = null;
    private final List<Entry> entries = new ArrayList<>();

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
                os.writeInt(entry.mbr.x);
                os.writeInt(entry.mbr.y);
                os.writeInt(entry.mbr.width);
                os.writeInt(entry.mbr.height);
                os.writeLong(entry.childOffset);
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

    boolean isRoot() {
        return parentOffset == -1;
    }

    boolean isLeaf() {
        return entries.isEmpty() || entries.get(0).childOffset < 0;
    }

    boolean isFull() {
        return entries.size() >= RStarTree.MAX_ENTRIES;
    }

    Node addNode(Node node) {
        if (isFull()) {
            throw new IllegalStateException("entries overflow");
        }

        entries.add(new Entry(storage, node.getMbr(), node.getOffset()));
        // putEntry(Entry.of(buffer, node.getMbr(), node.getOffset()));

        node.parentOffset = offset;
        mbr = null;

        node.save();
        save();

        return this;
    }

    Rectangle getMbr() {
        if (mbr == null) {

            mbr = entries.isEmpty() ? new Rectangle() : entries.get(0).mbr;
            for (Entry e : entries) {
                mbr = mbr.union(e.mbr);
            }
        }
        return mbr;
    }

    void resetMbr() {
        mbr = null;
    }

    void setOffset(long offset) {
        this.offset = offset;
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

    Node addEntry(Entry entry) {
        putEntry(entry);
        save();
        return this;
    }

    private Node putEntry(Entry entry) {
        if (entries.size() >= RStarTree.MAX_ENTRIES) {
            throw new IllegalStateException("entries overflow");
        }

        entries.add(entry);
        mbr = null;

        Node node = entry.loadNode();
        if (node != null) {
            node.parentOffset = offset;
            node.save();
        }

        return this;
    }

    Node setEntries(List<Entry> entries) {
        this.entries.clear();
        for (Entry e: entries) {
            putEntry(e);
        }
        // TODO: recalc MBR
        mbr = null;
        save(); // TODO:
        return this;
    }

}

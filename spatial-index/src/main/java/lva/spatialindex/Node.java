package lva.spatialindex;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author vlitvinenko
 */
class Node {
    private final NodeStorage storage;
    private long offset; // 0 ... n
    private long parentOffset = -1;

    private Rectangle mbr = null;
    private final List<Entry> entries = new ArrayList<>();

    public Node(NodeStorage storage, long offset) {
        this.storage = storage; // TODO: rename to buffer
        this.offset = offset;
    }

    public Node save() throws Exception {
        storage.write(offset, this);
        return this;
    }

    byte[] serialize() throws IOException {
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
    }

//    DirectArray serialize() throws IOException {
//        DirectArray buff = new DirectArray(SIZE);
//        DirectOutputStream os = new DirectOutputStream(buff);
//
//        os.writeLong(parentOffset);
//        os.writeInt(entries.size());
//
//        for (Entry entry: entries) {
//            os.writeInt(entry.mbr.x);
//            os.writeInt(entry.mbr.y);
//            os.writeInt(entry.mbr.width);
//            os.writeInt(entry.mbr.height);
//            os.writeLong(entry.childOffset);
//        }
//
//        return buff;
//    }

    Node deserialize(byte[] buff) throws IOException {
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
    }

//    void deserialize(DirectArray buff) throws IOException {
//        DirectInputStream is = new DirectInputStream(buff);
//
//        parentOffset = is.readLong();
//
//        int entriesSize = is.readInt();
//        entries.clear();
//
//        for (int i = 0; i < entriesSize; i++) {
//            int x = is.readInt();
//            int y = is.readInt();
//            int width = is.readInt();
//            int height = is.readInt();
//            long childOffset = is.readLong();
//            entries.add(new Entry(storage, new Rectangle(x, y, width, height), childOffset));
//        }
//
//    }

    boolean isRoot() {
        return parentOffset == -1;
    }

    boolean isLeaf() {
        return entries.isEmpty() || entries.get(0).childOffset < 0;
    }

    boolean isFull() {
        return entries.size() >= RStarTree.MAX_ENTRIES;
    }

    Node addNode(Node node) throws Exception {
        if (isFull()) {
            throw new IllegalStateException("entries overflow");
        }

        entries.add(Entry.of(storage, node.getMbr(), node.getOffset()));
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

    Node addEntry(Entry entry) throws Exception {
        putEntry(entry);
        save();
        return this;
    }

    private Node putEntry(Entry entry) throws Exception {
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

    Node setEntries(List<Entry> entries) throws Exception {
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

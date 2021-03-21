package lva.spatialindex.index;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lva.spatialindex.storage.Storage;
import lva.spatialindex.utils.Exceptions;

import java.awt.*;
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
    static final int PAGE_SIZE = 4096;
    static final int MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1;
    static final int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

    private final Storage<Node> storage;
    private long offset;
    private long parentOffset = -1;

    private Rectangle mbr = null;
    private final List<Entry> entries = new ArrayList<>();

    private Node(Storage<Node> storage, long offset) {
        this.storage = storage;
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
        return Exceptions.toRuntime(() -> {

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
        return Exceptions.toRuntime(() -> {

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
        Preconditions.checkState(!isFull(), "Entries overflow");

        entries.add(entry);
        resetMbr();

        entry.getChildNode().ifPresent(childNode -> {
            childNode.parentOffset = offset;
            childNode.save();
        });

        return this;
    }
}

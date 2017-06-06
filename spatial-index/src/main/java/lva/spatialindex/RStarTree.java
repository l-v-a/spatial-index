package lva.spatialindex;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static lva.spatialindex.Helpers.*;


// TODO: rename, think about Closeable
/*class NodeStorage implements AutoCloseable {
    private static final int RECORD_SIZE = 4096;
    private final MemoryMappedFile storage;
    private final LoadingCache<Long, Node> cache;

    NodeStorage(String fileName, long initialSize) throws Exception {
        this.storage = new MemoryMappedFile(fileName, initialSize);

        cache = CacheBuilder.newBuilder()
            .softValues()
            //.maximumSize(10000)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                new CacheLoader<Long, Node>() {
                    @Override
                    public Node load(@Nonnull Long offset) throws Exception {
                        return read(offset);
                    }
                });
    }

//    public long add(Node node) throws Exception {
//        try(DirectArray buff = node.serialize()) {
//            if (buff.getSize() > RECORD_SIZE) {
//                throw new IllegalArgumentException("record max size exceeds");
//            }
//
//            long offset = storage.allocate(buff.getSize(), (x) -> (x + 0xFFFL) & ~0xFFFL);
//            storage.setArray(offset, buff);
//
//            node.setOffset(offset);
//            cache.put(node.getOffset(), node);
//
//            return offset;
//        }
//    }

    public long add(Node node) throws Exception {
        byte[] buff = node.serialize();
        if (buff.length > RECORD_SIZE) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        long offset = storage.allocate(buff.length, (x) -> (x + 0xFFFL) & ~0xFFFL);
        storage.setBytes(offset, buff);

        node.setOffset(offset);
        cache.put(node.getOffset(), node);

        return offset;

    }

//    public void write(Node node) throws Exception {
//        try(DirectArray buff = node.serialize()) {
//            if (buff.getSize() > RECORD_SIZE) {
//                throw new IllegalArgumentException("record max size exceeds");
//            }
//
//            if (node.getOffset() < 0) {
//                throw new IllegalArgumentException("record was not allocated");
//            }
//
//            storage.setArray(node.getOffset(), buff);
//        }
//    }

    public void write(Node node) throws Exception {
        byte[] buff = node.serialize();
        if (buff.length > RECORD_SIZE) {
            throw new IllegalArgumentException("record max size exceeds");
        }

        if (node.getOffset() < 0) {
            throw new IllegalArgumentException("record was not allocated");
        }

        storage.setBytes(node.getOffset(), buff);

    }

//    public Node read(long offset) throws Exception {
//        try(DirectArray buff = new DirectArray(RECORD_SIZE)) {
//            if (offset + buff.getSize() > storage.getSize()) {
//                throw new IllegalArgumentException("out of bounds");
//            }
//
//            storage.getArray(offset, buff);
//            Node node = new Node(this, offset);
//            node.deserialize(buff);
//
//            return node;
//        }
//    }

    public Node read(long offset) throws Exception {
        byte[] buff = new byte[RECORD_SIZE];
        if (offset + buff.length > storage.getSize()) {
            throw new IllegalArgumentException("out of bounds");
        }

        storage.getBytes(offset, buff);
        Node node = new Node(this, offset);
        node.deserialize(buff);

        return node;
    }

    // TODO: remove
    Node newNode() throws Exception {
        Node node = new Node(this, -1);
        add(node);
        return node;
    }

    Node get(long offset) {
        try {
            return cache.get(offset);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        this.storage.close();
    }
}
*/

// TODO: rename to IndexTree
public class RStarTree implements AutoCloseable {
    public static int PAGE_SIZE = 4096; // TODO: calculate
    public static int MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1;
    public static int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

//    public static int MAX_ENTRIES = 30;
//    public static int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

//    public static int MAX_ENTRIES = 3;
//    public static int MIN_ENTRIES = 1;

    private NodeStorage storage;
    private Node root;


    RStarTree(int maxNumberOfElements, String storageFileName) throws Exception {
        long size = 64 * 1024L * 1024L; // TODO: make it as func (maxNumberOfElements)
        this.storage = new NodeStorage(storageFileName, size);
        this.root = storage.newNode();

    }

    public Collection<Long> search(Rectangle area) throws Exception {
        return search(root, area);
    }

    Collection<Long> search(Node node, Rectangle area) throws Exception {
        Collection<Long> res = new HashSet<>();
        if (node.isLeaf()) {
            for (Entry e: node.getEntries()) {
                if (area.intersects(e.mbr)) {
                    res.add(-(e.getChildOffset() + 1));
                }
            }
        } else {
            for (Entry e: node.getEntries()) {
                if (area.intersects(e.mbr)) {
                    res.addAll(search(e.loadNode(), area));
                }
            }
        }

        return res;
    }

    // TODO: think about common interface with DB
    public void insert(long offset, Rectangle newMbr) throws Exception {
        if (!newMbr.isEmpty()) {
            insert(root, offset, newMbr);
        }
    }

    void insert(Node node, long offset, Rectangle newMbr) throws Exception {
        Node leafNode = chooseLeaf(node, newMbr);
        Node newNode = null;

        Entry entry = Entry.of(storage, newMbr, -(offset + 1));
        if (!leafNode.isFull()) {
            leafNode.addEntry(entry);
        } else {
            newNode = splitNode(leafNode, storage.newNode().addEntry(entry));
        }

        adjust(leafNode, newNode);
    }

    Node chooseLeaf(Node node, Rectangle newMbr) throws Exception {
        if (node.isLeaf()) {
            return node;
        }

        // check that points to leaf node
        // TODO: checks may be omitted beacause its not lead node

        List<Entry> candidates = new ArrayList<>();

        if (!node.getEntries().isEmpty() && node.getEntries().get(0).loadNode().isLeaf()) {
            // points to leafs

            // min overlap cost
            candidates = minList(node.getEntries(), e -> area(e.mbr.intersection(newMbr)));

            // by min enlarged
            candidates = minList(candidates, e -> area(e.mbr.union(newMbr)));

            // by size
            candidates = minList(candidates, e -> area(e.mbr));


        } else {
            // not points to leaf

            // by min enlarged
            candidates = minList(node.getEntries(), e -> area(e.mbr.union(newMbr)));

            // by size
            candidates = minList(candidates, e -> area(e.mbr));
        }

        node = candidates.isEmpty() ? null : candidates.get(0).loadNode();
        return chooseLeaf(node, newMbr);
    }

    Node splitNode(Node node, Node newNode) throws Exception {
        if (node.isLeaf() != newNode.isLeaf()) {
            return newNode;
        }

        // distribute entries between nodes

        List<Entry> allEntries = new ArrayList<>(node.getEntries().size() + newNode.getEntries().size());
        allEntries.addAll(node.getEntries());
        allEntries.addAll(newNode.getEntries());

        // sort by x axis
        List<Entry> sort1ByX = new ArrayList<>(allEntries);
        sort1ByX.sort(LEFT_TO_RIGHT_BY_LEFT_COMPARATOR);
        List<Entry> sort2ByX = new ArrayList<>(allEntries);
        sort2ByX.sort(LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR);

        // sort by y axis
        List<Entry> sort1ByY = new ArrayList<>(allEntries);
        sort1ByY.sort(TOP_TO_BOTTOM_TOP_COMPARATOR);
        List<Entry> sort2ByY = new ArrayList<>(allEntries);
        sort2ByY.sort(TOP_TO_BOTTOM_BOTTOM_COMPARATOR);

        List<GroupPair> groups1X = getDistributions(sort1ByX);
        List<GroupPair> groups2X = getDistributions(sort2ByX);
        List<GroupPair> groups1Y = getDistributions(sort1ByY);
        List<GroupPair> groups2Y = getDistributions(sort2ByY);

        int marginX = marginGroups(groups1X) + marginGroups(groups2X);
        int marginY = marginGroups(groups1Y) + marginGroups(groups2Y);

        List<GroupPair> axisGroups = new ArrayList<>();
        axisGroups.addAll(marginX < marginY ? groups1X : groups1Y);
        axisGroups.addAll(marginX < marginY ? groups2X : groups2Y);


        // find min overlapped values distribution
        List<GroupPair> candidatesOverlapped = minList(axisGroups, g -> {
            Rectangle gr1 = union(g.group1);
            Rectangle gr2 = union(g.group2);
            return area(gr1.intersection(gr2));
        });

        candidatesOverlapped = minList(candidatesOverlapped,
            g -> area(union(g.group1)) + area(union(g.group2)));

        GroupPair pair = candidatesOverlapped.isEmpty() ? new GroupPair()
            : candidatesOverlapped.get(0);

        node.setEntries(pair.group1);
        newNode.setEntries(pair.group2);

        return newNode;
    }

    void adjust(Node node1, Node node2) throws Exception {

        if (node1.getOffset() == root.getOffset()) {
            if (node2 != null) {
                root = storage.newNode()
                    .addNode(node1)
                    .addNode(node2);
            }
            return;
        }

        Node parent = storage.get(node1.getParentOffset());
        Entry parentEntry = null;

        for (Entry e : parent.getEntries()) {
            if (e.loadNode().getOffset() == node1.getOffset()) {
                parentEntry = e;
                break;
            }
        }

        if (parentEntry != null) {
            parentEntry.mbr = node1.getMbr();
            parent.resetMbr();
            parent.save();
        }

        Node newNode = null;
        if (node2 != null) {
            if (!parent.isFull()) {
                parent.addNode(node2);
            } else {
                newNode = splitNode(parent, storage.newNode().addNode(node2));
            }
        }

        node1 = parent;
        node2 = newNode;

        adjust(node1, node2);
    }

    void dump() throws Exception {
        dump(root, "");
    }
    static void dump(Node node, String indent) throws Exception {
        System.out.printf("%s[node: %s]%n", indent, node.getOffset());
        System.out.printf("%s[entries:]", indent);
        for (Entry e: node.getEntries()) {

            System.out.printf("%s[e: %s, mbr: %s]", indent, e.childOffset, e.mbr);
            // System.out.printf("%s%n", indent);
        }
        System.out.printf("%s%n", indent);

        for (Entry e: node.getEntries()) {
            Node child = e.loadNode();
            if (child != null) {
                dump(child, indent + "\t");
            }
        }


    }
    void dump(Graphics g) throws Exception {
        g.setColor(Color.BLUE);
        drawNode(root, g, 0);
    }


    private static Color COLORS[] = {Color.BLACK, Color.BLUE, Color.RED, Color.GRAY, Color.YELLOW};
    void drawNode(Node node, Graphics g, int level) throws Exception {
        for (Entry entry : node.getEntries()) {
            if (node.isLeaf()) {
                g.setColor(Color.RED);
                g.fillRect(entry.mbr.x, entry.mbr.y, entry.mbr.width, entry.mbr.height);
                g.setColor(Color.RED);
                g.drawRect(entry.mbr.x, entry.mbr.y, entry.mbr.width, entry.mbr.height);
            } else {
                //g.setColor(Color.RED);
                //g.drawRect(entry.mbr.x, entry.mbr.y, entry.mbr.width, entry.mbr.height);
            }
            if (entry.loadNode() != null) {
                drawNode(entry.loadNode(), g, level + 1);
            }
            //g.setColor(c);
            //g.drawRect(entry.mbr.x, entry.mbr.y, entry.mbr.width, entry.mbr.height);
        }

        Rectangle mbr = node.getMbr();

        g.setColor(Color.BLACK);
        g.drawRect(mbr.x, mbr.y, mbr.width, mbr.height);
        g.drawRect(mbr.x + 1 , mbr.y + 1, mbr.width - 2, mbr.height - 2);

    }

    @Override
    public void close() throws Exception {
        storage.close();
    }
}

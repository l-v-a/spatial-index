package lva.spatialindex.index;

import lva.spatialindex.Storage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


/**
 * @author vlitvinenko
 */
public class RStarTree implements AutoCloseable {
    static final int PAGE_SIZE = 4096; // TODO: calculate
    static final int MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1;
    static final int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

//    public static int MAX_ENTRIES = 30;
//    public static int MIN_ENTRIES = MAX_ENTRIES * 2 / 5;

//    public static int MAX_ENTRIES = 3;
//    public static int MIN_ENTRIES = 1;

    private final Storage<Node> storage;
    private Node root;


    public RStarTree(int maxNumberOfElements, String storageFileName) {
        long size = 64 * 1024L * 1024L; // TODO: make it as func (maxNumberOfElements)
        this.storage = new NodeStorage(storageFileName, size);
        this.root = Node.newNode(this.storage);
    }

    public Collection<Long> search(Rectangle area) {
        return search(root, area);
    }

    Collection<Long> search(Node node, Rectangle area) {
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
    public void insert(long offset, Rectangle newMbr) {
        if (!newMbr.isEmpty()) {
            insert(root, offset, newMbr);
        }
    }

    void insert(Node node, long offset, Rectangle newMbr) {
        Node leafNode = chooseLeaf(node, newMbr);
        Node newNode = null;

        Entry entry = Entry.of(storage, newMbr, -(offset + 1));
        if (!leafNode.isFull()) {
            leafNode.addEntry(entry);
        } else {
            newNode = splitNode(leafNode, Node.newNode(storage).addEntry(entry));
        }

        adjust(leafNode, newNode);
    }

    Node chooseLeaf(Node node, Rectangle newMbr) {
        if (node.isLeaf()) {
            return node;
        }

        // check that points to leaf node
        // TODO: checks may be omitted beacause its not lead node

        List<Entry> candidates = new ArrayList<>();

        if (!node.getEntries().isEmpty() && node.getEntries().get(0).loadNode().isLeaf()) {
            // points to leafs

            // min overlap cost
            candidates = Helpers.minList(node.getEntries(), e -> Helpers.area(e.mbr.intersection(newMbr)));

            // by min enlarged
            candidates = Helpers.minList(candidates, e -> Helpers.area(e.mbr.union(newMbr)));

            // by size
            candidates = Helpers.minList(candidates, e -> Helpers.area(e.mbr));


        } else {
            // not points to leaf

            // by min enlarged
            candidates = Helpers.minList(node.getEntries(), e -> Helpers.area(e.mbr.union(newMbr)));

            // by size
            candidates = Helpers.minList(candidates, e -> Helpers.area(e.mbr));
        }

        node = candidates.isEmpty() ? null : candidates.get(0).loadNode();
        return chooseLeaf(node, newMbr);
    }

    Node splitNode(Node node, Node newNode) {
        if (node.isLeaf() != newNode.isLeaf()) {
            return newNode;
        }

        // distribute entries between nodes

        List<Entry> allEntries = new ArrayList<>(node.getEntries().size() + newNode.getEntries().size());
        allEntries.addAll(node.getEntries());
        allEntries.addAll(newNode.getEntries());

        // sort by x axis
        List<Entry> sort1ByX = new ArrayList<>(allEntries);
        sort1ByX.sort(Entry.LEFT_TO_RIGHT_BY_LEFT_COMPARATOR);
        List<Entry> sort2ByX = new ArrayList<>(allEntries);
        sort2ByX.sort(Entry.LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR);

        // sort by y axis
        List<Entry> sort1ByY = new ArrayList<>(allEntries);
        sort1ByY.sort(Entry.TOP_TO_BOTTOM_TOP_COMPARATOR);
        List<Entry> sort2ByY = new ArrayList<>(allEntries);
        sort2ByY.sort(Entry.TOP_TO_BOTTOM_BOTTOM_COMPARATOR);

        List<Helpers.GroupPair> groups1X = Helpers.getDistributions(sort1ByX);
        List<Helpers.GroupPair> groups2X = Helpers.getDistributions(sort2ByX);
        List<Helpers.GroupPair> groups1Y = Helpers.getDistributions(sort1ByY);
        List<Helpers.GroupPair> groups2Y = Helpers.getDistributions(sort2ByY);

        int marginX = Helpers.marginGroups(groups1X) + Helpers.marginGroups(groups2X);
        int marginY = Helpers.marginGroups(groups1Y) + Helpers.marginGroups(groups2Y);

        List<Helpers.GroupPair> axisGroups = new ArrayList<>();
        axisGroups.addAll(marginX < marginY ? groups1X : groups1Y);
        axisGroups.addAll(marginX < marginY ? groups2X : groups2Y);


        // find min overlapped values distribution
        List<Helpers.GroupPair> candidatesOverlapped = Helpers.minList(axisGroups, g -> {
            Rectangle gr1 = Helpers.union(g.group1);
            Rectangle gr2 = Helpers.union(g.group2);
            return Helpers.area(gr1.intersection(gr2));
        });

        candidatesOverlapped = Helpers.minList(candidatesOverlapped,
            g -> Helpers.area(Helpers.union(g.group1)) + Helpers.area(Helpers.union(g.group2)));

        Helpers.GroupPair pair = candidatesOverlapped.isEmpty() ? new Helpers.GroupPair()
            : candidatesOverlapped.get(0);

        node.setEntries(pair.group1);
        newNode.setEntries(pair.group2);

        return newNode;
    }

    void adjust(Node node1, Node node2) {

        if (node1.getOffset() == root.getOffset()) {
            if (node2 != null) {
                root = Node.newNode(storage)
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
                newNode = splitNode(parent, Node.newNode(storage).addNode(node2));
            }
        }

        node1 = parent;
        node2 = newNode;

        adjust(node1, node2);
    }

    @Override
    public void close() {
        storage.close();
    }
}

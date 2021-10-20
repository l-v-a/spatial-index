package lva.spatialindex.index;

import lva.spatialindex.index.Distributions.GroupPair;
import lva.spatialindex.storage.Storage;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static lva.spatialindex.index.Distributions.getDistributionGroups;
import static lva.spatialindex.index.Distributions.getGroupMargins;
import static lva.spatialindex.index.Entry.union;
import static lva.spatialindex.index.RStarTree.Utils.minList;
import static lva.spatialindex.index.Rectangles.area;


/**
 * @author vlitvinenko
 */
public class RStarTree implements Index {
    private final Storage<Node> storage;
    private Node root;

    public RStarTree(int maxNumberOfElements, String storageFileName) {
        long size = 64 * 1024L * 1024L; // TODO: make it as func (maxNumberOfElements)
        this.storage = new NodeStorage(storageFileName, size); // TODO: move out creation
        this.root = Node.newNode(this.storage);
    }

    @Override
    public Collection<Long> search(Rectangle area) {
        return search(root, area).collect(toList());
    }

    private Stream<Long> search(Node node, Rectangle area) {
        return node.getEntries().stream().filter(entry -> area.intersects(entry.getMbr()))
                .flatMap(entry ->
                        entry.data().map(value -> Stream.of(-(value + 1)))
                                .getOrElseGet(childNode -> search(childNode, area))
                );
    }

    public void insert(long offset, Rectangle newMbr) {
        checkArgument(!newMbr.isEmpty(), "Invalid region");
        insert(root, offset, newMbr);
    }

    private void insert(Node node, long offset, Rectangle newMbr) {
        Node leafNode = chooseSubtree(node, newMbr);
        Node newNode = null;

        Entry entry = new Entry(storage, newMbr, -(offset + 1));
        if (!leafNode.isFull()) {
            leafNode.addEntry(entry);
        } else {
            newNode = splitNode(leafNode, Node.newNode(storage).addEntry(entry));
        }

        adjust(leafNode, newNode);
    }

    private static Node chooseSubtree(Node node, Rectangle newMbr) {
        if (node.isLeaf()) {
            return node;
        }

        Function<Entry, Long> bySize = e -> area(e.getMbr());
        Function<Entry, Long> byEnlarge = e -> area(e.getMbr().union(newMbr));
        Function<Entry, Long> byCost = e -> area(e.getMbr().intersection(newMbr));

        List<Entry> candidates = node.getEntries();

        // check that points to leaf node
        boolean isContainsLeaf = node.getEntries().stream().findAny()
                .flatMap(Entry::getChildNode)
                .map(Node::isLeaf).orElse(false);

        if (isContainsLeaf) {
            candidates = minList(candidates, byCost);
        }

        candidates = minList(candidates, byEnlarge);
        candidates = minList(candidates, bySize);

        return candidates.stream().findAny().flatMap(Entry::getChildNode)
                .map(childNode -> chooseSubtree(childNode, newMbr))
                .orElse(node);
    }

    private static Node splitNode(Node node, Node newNode) {
        if (node.isLeaf() != newNode.isLeaf()) {
            return newNode;
        }

        // distribute entries between nodes
        var allEntries = new ArrayList<>(node.getEntries());
        allEntries.addAll(newNode.getEntries());

        var groupsX = getDistributionGroups(allEntries, Entry.X_COMPARATORS);
        var groupsY = getDistributionGroups(allEntries, Entry.Y_COMPARATORS);
        var groups = getGroupMargins(groupsX) < getGroupMargins(groupsY) ? groupsX : groupsY;

        // find min overlapped values distribution
        var overlapped = groups.stream().flatMap(Collection::stream).collect(toList());
        overlapped = minList(overlapped, pair -> area(union(pair.group1).intersection(union(pair.group2))));
        overlapped = minList(overlapped, pair -> area(union(pair.group1)) + area(union(pair.group2)));

        var pair = overlapped.stream().findAny().orElse(new GroupPair());

        node.setEntries(pair.group1);
        newNode.setEntries(pair.group2);

        return newNode;
    }

    private void adjust(Node node1, Node node2) {
        if (node1.getOffset() == root.getOffset()) {
            // node1 is root node
            if (node2 != null) {
                root = Node.newNode(storage)
                    .addNode(node1)
                    .addNode(node2);
            }
            return;
        }

        Node parent = storage.read(node1.getParentOffset());
        syncMbr(parent, node1);

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

    private static void syncMbr(Node targetNode, Node sourceNode) {
        Predicate<Entry> isTargetEntry = entry -> entry.getChildNode()
                .map(Node::getOffset)
                .map(offset -> offset == sourceNode.getOffset()).orElse(false);

        Optional<Entry> targetEntry = targetNode.getEntries().stream()
                .filter(isTargetEntry).findAny();

        targetEntry.ifPresent(entry -> {
            entry.setMbr(sourceNode.getMbr());
            targetNode.resetMbr()
                    .save();
        });
    }

    @Override
    public void close() {
        storage.close();
    }

    static class Utils {
        static <T> List<T> minList(List<? extends T> list, Function<? super T, Long> criteria) {
            List<T> candidates = new ArrayList<>();
            long minValue = Integer.MAX_VALUE;

            for (T e : list) {
                long ovrArea = criteria.apply(e);
                if (ovrArea <= minValue) {
                    if (ovrArea < minValue)  {
                        candidates.clear();
                        minValue = ovrArea;
                    }
                    candidates.add(e);
                }
            }

            return candidates;
        }
    }
}

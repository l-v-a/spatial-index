package lva.spatialindex.index;

import java.util.ArrayList;
import java.util.List;

import static lva.spatialindex.index.Entry.margin;

/**
 * @author vlitvinenko
 */
class Distributions {
    private Distributions() {}

    static class GroupPair {
        List<Entry> group1 = new ArrayList<>();
        List<Entry> group2 = new ArrayList<>();
    }

    static int marginGroups(List<GroupPair> groups) {
        int margin = 0;
        for (GroupPair g: groups) {
            margin += margin(g.group1) + margin(g.group2);
        }
        return margin;
    }

    static List<GroupPair> getDistributions(List<Entry> entries) {
        List<GroupPair> groups = new ArrayList<>();
        for (int k = 0; k < (Node.MAX_ENTRIES - 2* Node.MIN_ENTRIES + 2); k++) {
            GroupPair pair = new GroupPair();
            pair.group1 = entries.subList(0, Node.MIN_ENTRIES + k);
            pair.group2 = entries.subList(Node.MIN_ENTRIES + k, entries.size());
            groups.add(pair);
        }
        return groups;
    }
}

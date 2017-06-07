package lva.spatialindex.index;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author vlitvinenko
 */
class Helpers {
    private Helpers() {}

    static long area(Rectangle r) {
        return r.width * r.height;
    }

    static int margin(Rectangle r) {
        return 2 * (r.height + r.width);
    }

    static Rectangle union(List<Entry> entries) { // TODO: use Collection or stream
        Rectangle r = entries.isEmpty() ? new Rectangle() : entries.get(0).mbr;
        for (Entry e: entries) {
            r = r.union(e.mbr);
        }
        return r;
    }

    static int margin (List<Entry> entries) {
        int margin = 0;
        for (Entry e: entries) {
            margin += margin(e.mbr);
        }
        return margin;
    }

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
        for (int k = 0; k < (RStarTree.MAX_ENTRIES - 2* RStarTree.MIN_ENTRIES + 2); k++) {
            GroupPair pair = new GroupPair();
            pair.group1 = entries.subList(0, RStarTree.MIN_ENTRIES + k);
            pair.group2 = entries.subList(RStarTree.MIN_ENTRIES + k, entries.size());
            groups.add(pair);
        }
        return groups;
    }

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

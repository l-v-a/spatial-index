package lva.spatialindex.index;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * @author vlitvinenko
 */
class Helpers {

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_LEFT_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.x, e2.mbr.x);
    };

    static final Comparator<Entry> LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.x + e1.mbr.width, e2.mbr.x + e2.mbr.width);
    };

    static final Comparator<Entry> TOP_TO_BOTTOM_TOP_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.y, e2.mbr.y);
    };

    static final Comparator<Entry> TOP_TO_BOTTOM_BOTTOM_COMPARATOR = (e1, e2) -> {
        return Integer.compare(e1.mbr.y + e1.mbr.height, e2.mbr.y + e2.mbr.height);
    };

    static long area(Rectangle r) {
        return r.width * r.height;
    }

    static Rectangle union(List<Entry> entries) {
        Rectangle r = entries.isEmpty() ? new Rectangle() : entries.get(0).mbr;
        for (Entry e: entries) {
            r = r.union(e.mbr);
        }
        return r;
    }

    static int margin(Rectangle r) {
        return 2 * (r.height + r.width);
    }

    static int margin (List<Entry> entries) {
        int margin = 0;
        for (Entry e: entries) {
            margin += margin(e.mbr);
        }
        return margin;
    }

    static int marginGroups(List<GroupPair> groups) {
        int margin = 0;
        for (GroupPair g: groups) {
            margin += margin(g.group1) + margin(g.group2);
        }
        return margin;
    }

    static class GroupPair {
        List<Entry> group1 = new ArrayList<>();
        List<Entry> group2 = new ArrayList<>();
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

    static <T> List<T> minList(List<? extends T> entries, Function<? super T, Long> criteria) {
        List<T> candidates = new ArrayList<>();
        long minValue = Integer.MAX_VALUE;

        for (T e : entries) {
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

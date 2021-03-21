package lva.spatialindex.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
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
        return groups.stream().mapToInt(g -> margin(g.group1) + margin(g.group2))
                .sum();
    }

    static List<GroupPair> getDistributions(List<Entry> entries) {
        List<GroupPair> groups = new ArrayList<>();
        for (int k = 0; k < (Node.MAX_ENTRIES - 2 * Node.MIN_ENTRIES + 2); k++) {
            GroupPair pair = new GroupPair();
            pair.group1 = entries.subList(0, Node.MIN_ENTRIES + k);
            pair.group2 = entries.subList(Node.MIN_ENTRIES + k, entries.size());
            groups.add(pair);
        }
        return groups;
    }

    static int getMargins(List<List<GroupPair>> distributionGroups) {
        return distributionGroups.stream().mapToInt(Distributions::marginGroups).sum();
    }

    static List<List<GroupPair>> getDistributionGroups(List<Entry> entries,
                                                       Collection<Comparator<Entry>> comparators) {

        Function<Comparator<Entry>, List<Entry>> sorted = cmp ->
                entries.stream().sorted(cmp).collect(toList());

        return comparators.stream().map(sorted)
                .map(Distributions::getDistributions)
                .collect(toList());
    }
}

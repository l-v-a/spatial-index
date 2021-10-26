package lva.spatialindex.index

import lva.spatialindex.index.Entry.Companion.margin

/**
 * @author vlitvinenko
 */
internal fun getGroupMargins(distributionGroups: List<List<GroupPair>>): Int =
    distributionGroups.sumOf { margins(it) }

internal fun getDistributionGroups(entries: List<Entry>, comparators: Collection<Comparator<Entry>>): List<List<GroupPair>> =
     comparators.asSequence()
        .map { entries.sortedWith(it) }
        .map { distributions(it) }
        .toList()

private fun margins(groups: List<GroupPair>): Int =
    groups.sumOf { margin(it.group1) + margin(it.group2) }

private fun distributions(entries: List<Entry>): List<GroupPair> {
    val groups = arrayListOf<GroupPair>()
    for (k in 0 until Node.MAX_ENTRIES - 2 * Node.MIN_ENTRIES + 2) {
        val pair = GroupPair(
            entries.subList(0, Node.MIN_ENTRIES + k),
            entries.subList(Node.MIN_ENTRIES + k, entries.size)
        )
        groups.add(pair)
    }
    return groups
}

// TODO: use Pair
internal class GroupPair(val group1: List<Entry> = arrayListOf(), val group2: List<Entry> = arrayListOf())

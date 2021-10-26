package lva.spatialindex.index

import lva.spatialindex.index.Entry.Companion.margin

/**
 * @author vlitvinenko
 */

internal typealias GroupPair = Pair<List<Entry>, List<Entry>>
internal typealias DistributionGroups = List<GroupPair>

internal fun getGroupMargins(distributionGroups: List<DistributionGroups>): Int =
    distributionGroups.sumOf { margins(it) }

internal fun getDistributionGroups(entries: List<Entry>, comparators: Collection<Comparator<Entry>>): List<DistributionGroups> =
    comparators.asSequence()
        .map { entries.sortedWith(it) }
        .map { distributions(it) }
        .toList()

private fun margins(groups: DistributionGroups): Int =
    groups.sumOf { margin(it.first) + margin(it.second) }

private fun distributions(entries: List<Entry>): DistributionGroups {
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

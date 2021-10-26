package lva.spatialindex.index

import lva.spatialindex.index.Entry.Companion.margin

/**
 * @author vlitvinenko
 */

internal typealias GroupPair = Pair<List<Entry>, List<Entry>>

internal fun getGroupMargins(distributionGroups: List<List<GroupPair>>): Int =
    distributionGroups.sumOf { margins(it) }

internal fun getDistributionGroups(entries: List<Entry>, comparators: Collection<Comparator<Entry>>): List<List<GroupPair>> =
    comparators.asSequence()
        .map { entries.sortedWith(it) }
        .map { distributions(it) }
        .toList()

private fun margins(groups: List<GroupPair>): Int =
    groups.sumOf { margin(it.first) + margin(it.second) }

private fun distributions(entries: List<Entry>): List<GroupPair> =
    (0 until Node.MAX_ENTRIES - 2 * Node.MIN_ENTRIES + 2).map { k ->
        GroupPair(
            entries.subList(0, Node.MIN_ENTRIES + k),
            entries.subList(Node.MIN_ENTRIES + k, entries.size)
        )
    }.toList()


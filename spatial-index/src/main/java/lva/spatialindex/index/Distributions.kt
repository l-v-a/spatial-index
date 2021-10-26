package lva.spatialindex.index


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
    groups.sumOf { it.first.margin() + it.second.margin() }

private fun distributions(entries: List<Entry>): List<GroupPair> =
    (0 until Node.MAX_ENTRIES - 2 * Node.MIN_ENTRIES + 2).map { i ->
        GroupPair(
            entries.subList(0, Node.MIN_ENTRIES + i),
            entries.subList(Node.MIN_ENTRIES + i, entries.size)
        )
    }.toList()


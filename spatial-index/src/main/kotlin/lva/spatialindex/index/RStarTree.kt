package lva.spatialindex.index

import arrow.core.getOrHandle
import lva.spatialindex.index.Node.Companion.newNode
import java.awt.Rectangle
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author vlitvinenko
 */
class RStarTree(maxNumberOfElements: Int, storageFileName: String) : Index {
    private val storage = NodeStorage(storageFileName, SIZE_DEFAULT) // TODO: move out creation
    private val lock = ReentrantReadWriteLock()
    private var root: Node = newNode(storage)

    override fun search(area: Rectangle): Collection<Long> {
        lock.read {
            return search(root, area).toList()
        }
    }

    private fun search(node: Node, area: Rectangle): Sequence<Long> =
        node.entries.asSequence()
            .filter { entry ->
                area.intersects(entry.mbr)
            }
            .flatMap { entry ->
                entry.data()
                    .map { value -> sequenceOf(-(value + 1)) }
                    .getOrHandle { childNode -> search(childNode, area) }
            }

    fun insert(offset: Long, newMbr: Rectangle) {
        check(!newMbr.isEmpty) { "Invalid region" }
        lock.write {
            insert(root, offset, newMbr)
        }
    }

    private fun insert(node: Node, offset: Long, newMbr: Rectangle) {
        val leafNode = chooseSubtree(node, newMbr)
        val entry = Entry(storage, newMbr, -(offset + 1))
        val newNode = when {
            leafNode.isFull -> splitNode(leafNode, newNode(storage).addEntry(entry))
            else -> null.also { leafNode.addEntry(entry) }
        }

        adjust(leafNode, newNode)
    }

    private fun adjust(node1: Node, node2: Node?) {
        if (node1.offset == root.offset) {
            // node1 is root node
            if (node2 != null) {
                root = newNode(storage)
                    .addNode(node1).addNode(node2)
            }

        } else {

            val parent = storage.read(node1.parentOffset)
            syncMbr(parent, node1)

            var newNode: Node? = null
            if (node2 != null) {
                if (parent.isFull) {
                    newNode = splitNode(parent, newNode(storage).addNode(node2))
                } else {
                    parent.addNode(node2)
                }
            }

            adjust(parent, newNode)
        }
    }

    private fun chooseSubtree(node: Node, newMbr: Rectangle): Node {
        if (node.isLeaf) {
            return node
        }

        var candidates: List<Entry> = node.entries
        val isContainsLeaf = node.entries.firstOrNull()?.childNode?.isLeaf ?: false

        if (isContainsLeaf) {
            candidates = candidates.minList { it.mbr.intersection(newMbr).area } // by cost
        }

        candidates = candidates.minList { it.mbr.union(newMbr).area } // by enlarge
        candidates = candidates.minList { it.mbr.area } // by size

        return candidates.firstOrNull()?.childNode?.let { chooseSubtree(it, newMbr) } ?: node
    }

    private fun splitNode(node: Node, newNode: Node): Node {
        if (node.isLeaf != newNode.isLeaf) {
            return newNode
        }

        // distribute entries between nodes
        val allEntries = node.entries + newNode.entries

        val groupsX = getDistributionGroups(allEntries, listOf(Entry::left, Entry::right))
        val groupsY = getDistributionGroups(allEntries, listOf(Entry::bottom, Entry::top))
        val groups = if (getGroupMargins(groupsX) < getGroupMargins(groupsY))
            groupsX else groupsY

        // find min overlapped values distribution
        var overlapped = groups.flatten()
        overlapped = overlapped.minList { it.first.union().intersection(it.second.union()).area }
        overlapped = overlapped.minList { it.first.union().area + it.second.union().area }

        val pair = overlapped.firstOrNull() ?: GroupPair(arrayListOf(), arrayListOf())
        node.setEntries(pair.first)
        newNode.setEntries(pair.second)

        return newNode
    }

    private fun syncMbr(targetNode: Node, sourceNode: Node) {
        val targetEntry = targetNode.entries.firstOrNull { entry ->
            entry.childNode?.offset == sourceNode.offset
        }

        targetEntry?.let { entry ->
            entry.mbr = sourceNode.mbr
            targetNode.resetMbr().save()
        }
    }

    fun clear() = lock.write {
        root.reset()
        storage.clear()
    }

    companion object {
        private const val SIZE_DEFAULT = 64 * 1024 * 1024
    }
}

val Rectangle.area: Long get() = (width * height).toLong()
val Rectangle.margin: Int get() = 2 * (height + width)


private inline fun <T> List<T>.minList(criteria: (T) -> Long): List<T> {
    val min = minOfOrNull(criteria)
    return min?.let { filter { criteria(it) == min } } ?: arrayListOf()
}


package lva.spatialindex.index

import lva.spatialindex.index.Entry.Companion.union
import lva.spatialindex.index.Node.Companion.newNode
import lva.spatialindex.storage.Storage
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
class RStarTree(maxNumberOfElements: Int, storageFileName: String) : Index {
    private val storage: Storage<Node> = NodeStorage(storageFileName, SIZE_DEFAULT) // TODO: move out creation
    private var root: Node = newNode(storage)

    override fun search(area: Rectangle): Collection<Long> =
        search(root, area).toList()

    private fun search(node: Node, area: Rectangle): Sequence<Long> =
        node.getEntries().asSequence()
            .filter { entry ->
                area.intersects(entry.mbr)
            }
            .flatMap { entry ->
                entry.data()
                    .map { value -> sequenceOf(-(value + 1)) }
                    .getOrElseGet { childNode -> search(childNode, area) }
            }


    fun insert(offset: Long, newMbr: Rectangle) {
        check(!newMbr.isEmpty) { "Invalid region" }
        insert(root, offset, newMbr)
    }

    private fun insert(node: Node, offset: Long, newMbr: Rectangle) {
        val leafNode = chooseSubtree(node, newMbr)
        val entry = Entry(storage, newMbr, -(offset + 1))
        val newNode = when {
            leafNode.isFull -> splitNode(leafNode, newNode(storage).addEntry(entry))
            else -> { leafNode.addEntry(entry); null }
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

        var candidates = node.getEntries()
        val isContainsLeaf = node.getEntries().firstOrNull()?.childNode?.isLeaf ?: false

        if (isContainsLeaf) {
            candidates = candidates.minList { it.mbr.intersection(newMbr).area() } // by cost
        }

        candidates = candidates.minList { it.mbr.union(newMbr).area() } // by enlarge
        candidates = candidates.minList { it.mbr.area() } // by size

        return candidates.firstOrNull()?.childNode?.let { chooseSubtree(it, newMbr) } ?: node
    }

    private fun splitNode(node: Node, newNode: Node): Node {
        if (node.isLeaf != newNode.isLeaf) {
            return newNode
        }

        // distribute entries between nodes
        val allEntries = node.getEntries() + newNode.getEntries()

        val groupsX = getDistributionGroups(allEntries, Entry.X_COMPARATORS)
        val groupsY = getDistributionGroups(allEntries, Entry.Y_COMPARATORS)
        val groups = if (getGroupMargins(groupsX) < getGroupMargins(groupsY))
            groupsX else groupsY

        // find min overlapped values distribution
        var overlapped = groups.flatten()
        overlapped = overlapped.minList { union(it.first).intersection(union(it.second)).area() }
        overlapped = overlapped.minList { union(it.first).area() + union(it.second).area() }

        val pair = overlapped.firstOrNull() ?: GroupPair(arrayListOf(), arrayListOf())
        node.setEntries(pair.first)
        newNode.setEntries(pair.second)

        return newNode
    }

    private fun syncMbr(targetNode: Node, sourceNode: Node) {
        val targetEntry = targetNode.getEntries().firstOrNull { entry ->
            entry.childNode?.offset == sourceNode.offset
        }

        targetEntry?.let { entry ->
            entry.mbr = sourceNode.getMbr()
            targetNode.resetMbr().save()
        }
    }

    override fun close() = storage.close()

    companion object {
        private const val SIZE_DEFAULT = 64 * 1024L * 1024L
    }
}

private inline fun <T> List<T>.minList(criteria: (T) -> Long): List<T> {
    val min = minOfOrNull(criteria)
    return min?.let { filter { criteria(it) == min } } ?: arrayListOf()
}


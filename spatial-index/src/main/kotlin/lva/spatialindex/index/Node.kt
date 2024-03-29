package lva.spatialindex.index

import lva.spatialindex.storage.Storage
import lva.spatialindex.utils.resettableWith
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
internal class Node(private val storage: Storage<Node>, var offset: Long) {
    var parentOffset = -1L
    val entries = mutableListOf<Entry>()
    val isLeaf get() = entries.firstOrNull()?.isLeaf ?: true
    val isFull get() = entries.size >= MAX_ENTRIES
    var mbr by resettableWith(NULL_RECTANGLE) { entries.union() }
        private set

    fun resetMbr(): Node = apply {
        mbr = NULL_RECTANGLE
    }

    fun addNode(node: Node): Node =
        addEntry(Entry(storage, node.mbr, node.offset))

    fun addEntry(entry: Entry): Node =
        putEntry(entry).save()

    fun setEntries(newEntries: List<Entry>): Node = apply {
        entries.clear()
        newEntries.forEach { putEntry(it) }
        save()
    }

    private fun putEntry(entry: Entry): Node = apply {
        check(!isFull) { "Entries overflow" }
        entries.add(entry)
        resetMbr()
        entry.childNode?.let { child ->
            child.parentOffset = offset
            child.save()
        }
    }

    fun save(): Node = apply { storage.write(offset, this) }

    fun reset(): Node = apply {
        parentOffset = -1
        resetMbr()
        entries.clear()
    }

    companion object {
        private val NULL_RECTANGLE = Rectangle()

        private const val PAGE_SIZE = 4096
        const val MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1
        const val MIN_ENTRIES = MAX_ENTRIES * 2 / 5

        fun newNode(storage: Storage<Node>) =
            Node(storage, -1).also { storage.add(it) }
    }
}

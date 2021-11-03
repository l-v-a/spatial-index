package lva.spatialindex.index

import lva.spatialindex.storage.Storage
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
internal class Node(private val storage: Storage<Node>, var offset: Long) {
    private var mbr: Rectangle = NULL_RECTANGLE
    internal val entries: MutableList<Entry> = ArrayList()

    var parentOffset: Long = -1
    val isLeaf get() = entries.firstOrNull()?.isLeaf ?: true
    val isFull get() = entries.size >= MAX_ENTRIES

    fun getMbr(): Rectangle { // TODO: refactor as property
        if (mbr === NULL_RECTANGLE)
            mbr = entries.union()
        return mbr
    }

    fun resetMbr(): Node = apply {
        mbr = NULL_RECTANGLE
    }

    fun getEntries(): List<Entry> = entries

    fun addNode(node: Node): Node =
        addEntry(Entry(storage, node.getMbr(), node.offset))

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
        entry.childNode?.let { childNode ->
            childNode.parentOffset = offset
            childNode.save()
        }
    }

    fun save(): Node = apply {
        storage.write(offset, this)
    }

    fun reset(): Node = apply {
        parentOffset = -1
        resetMbr()
        entries.clear()
    }

    companion object {
        private const val PAGE_SIZE = 4096
        const val MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1
        const val MIN_ENTRIES = MAX_ENTRIES * 2 / 5

        private val NULL_RECTANGLE = Rectangle()

        fun newNode(storage: Storage<Node>) = Node(storage, -1).apply {
            storage.add(this)
        }
    }
}
package lva.spatialindex.index

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import lva.spatialindex.storage.Storage
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
internal open class Node(private val storage: Storage<Node>, var offset: Long) {
    // TODO: fix open
    private val entries: MutableList<Entry> = ArrayList()

    var parentOffset: Long = -1
        private set

    private var mbr: Rectangle = NULL_RECTANGLE

    val isLeaf
        get() = entries.firstOrNull()?.isLeaf ?: true

    val isFull
        get() = entries.size >= MAX_ENTRIES

    fun getMbr(): Rectangle {
        mbr = if (mbr === NULL_RECTANGLE) Entry.union(entries) else mbr
        return mbr
    }

    fun resetMbr(): Node = apply { mbr = NULL_RECTANGLE }

    fun getEntries(): List<Entry> = entries

    fun addNode(node: Node): Node = addEntry(Entry(storage, node.getMbr(), node.offset))

    fun addEntry(entry: Entry): Node = putEntry(entry).save()

    fun setEntries(newEntries: List<Entry>): Node {
        entries.clear()
        newEntries.forEach { putEntry(it) }
        return save()
    }

    private fun putEntry(entry: Entry): Node = apply {
        check(!isFull) { "Entries overflow" }
        entries.add(entry)
        resetMbr()
        entry.childNode.ifPresent { childNode ->
            childNode.parentOffset = offset
            childNode.save()
        }
    }

    fun save(): Node = apply { storage.write(offset, this) }

    internal class Ser(private val storage: Storage<Node>) : Serializer<Node>() {
        override fun write(kryo: Kryo, output: Output, node: Node) {
            output.writeLong(node.parentOffset)
            output.writeInt(node.entries.size)
            node.entries.forEach {
                kryo.writeObject(output, it)
            }
        }

        override fun read(kryo: Kryo, input: Input, type: Class<Node>): Node = Node(storage, -1).apply {
            parentOffset = input.readLong()
            val entriesSize = input.readInt()
            repeat(entriesSize) {
                entries.add(kryo.readObject(input, Entry::class.java))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 4096
        const val MAX_ENTRIES = PAGE_SIZE / Entry.SIZE - 1
        const val MIN_ENTRIES = MAX_ENTRIES * 2 / 5

        private val NULL_RECTANGLE = Rectangle()

        @JvmStatic
        fun newNode(storage: Storage<Node>): Node = Node(storage, -1).apply { storage.add(this) }
    }
}
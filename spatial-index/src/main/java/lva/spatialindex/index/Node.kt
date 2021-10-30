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
internal class Node(private val storage: Storage<Node>, var offset: Long) {
    private val entries: MutableList<Entry> = ArrayList()
    private var mbr: Rectangle = NULL_RECTANGLE

    var parentOffset: Long = -1
        private set

    val isLeaf
        get() = entries.firstOrNull()?.isLeaf ?: true

    val isFull
        get() = entries.size >= MAX_ENTRIES

    fun getMbr(): Rectangle { // TODO: refactor as property
        if (mbr === NULL_RECTANGLE)
            mbr = entries.union()
        return mbr
    }

    fun resetMbr() = apply {
        mbr = NULL_RECTANGLE
    }

    fun getEntries(): List<Entry> = entries

    fun addNode(node: Node) =
        addEntry(Entry(storage, node.getMbr(), node.offset))

    fun addEntry(entry: Entry) =
        putEntry(entry).save()

    fun setEntries(newEntries: List<Entry>) = apply {
        entries.clear()
        newEntries.forEach { putEntry(it) }
        save()
    }

    private fun putEntry(entry: Entry) = apply {
        check(!isFull) { "Entries overflow" }
        entries.add(entry)
        resetMbr()
        entry.childNode?.let { childNode ->
            childNode.parentOffset = offset
            childNode.save()
        }
    }

    fun save() = apply {
        storage.write(offset, this)
    }

    internal class Ser(private val storage: Storage<Node>) : Serializer<Node>() {
        override fun write(kryo: Kryo, output: Output, node: Node) {
            output.writeLong(node.parentOffset)
            output.writeInt(node.entries.size)
            node.entries.forEach {
                kryo.writeObject(output, it)
            }
        }

        override fun read(kryo: Kryo, input: Input, type: Class<Node>) = Node(storage, -1).apply {
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

        fun newNode(storage: Storage<Node>) = Node(storage, -1).apply {
            storage.add(this)
        }
    }
}
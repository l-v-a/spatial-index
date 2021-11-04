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
internal class EntryKryoSerializer(private val storage: Storage<Node>) : Serializer<Entry>() {
    override fun write(kryo: Kryo, output: Output, entry: Entry) = with(output) {
        val mbr = entry.mbr
        writeInt(mbr.x)
        writeInt(mbr.y)
        writeInt(mbr.width)
        writeInt(mbr.height)
        writeLong(entry.childOffset)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Entry>): Entry = with(input) {
        val x = readInt()
        val y = readInt()
        val width = readInt()
        val height = readInt()
        val childOffset = readLong()
        Entry(storage, Rectangle(x, y, width, height), childOffset)
    }
}

internal class NodeKryoSerializer(private val storage: Storage<Node>) : Serializer<Node>() {
    override fun write(kryo: Kryo, output: Output, node: Node) {
        output.writeLong(node.parentOffset)
        output.writeInt(node.entries.size)
        node.entries.forEach {
            kryo.writeObject(output, it)
        }
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Node>) = Node(storage, -1).apply {
        parentOffset = input.readLong()
        val entriesSize = input.readInt()
        repeat(entriesSize) {
            entries.add(kryo.readObject(input, Entry::class.java))
        }
    }
}


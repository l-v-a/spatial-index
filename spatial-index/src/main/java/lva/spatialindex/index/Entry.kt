package lva.spatialindex.index

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import io.vavr.control.Either
import lva.spatialindex.storage.Storage
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
internal class Entry(private val storage: Storage<Node>, mbr: Rectangle, childOffset: Long) {
    private data class Body(var mbr: Rectangle, val childOffset: Long)
    private val body = Body(mbr, childOffset)

    var mbr: Rectangle by body::mbr
    val childOffset: Long by body::childOffset

    fun data(): Either<Node, Long> = if (childOffset < 0)
        Either.right(childOffset) else Either.left(storage.read(childOffset))

    val childNode: Node?
        get() = data().swap().getOrElseGet { null }

    val isLeaf
        get() = childOffset < 0

    override fun equals(other: Any?) = if (other is Entry) body == other.body else false
    override fun hashCode() = body.hashCode()

    internal class Ser(private val storage: Storage<Node>) : Serializer<Entry>() {
        override fun write(kryo: Kryo, output: Output, entry: Entry) = with(output) {
            val mbr = entry.mbr
            writeInt(mbr.x)
            writeInt(mbr.y)
            writeInt(mbr.width)
            writeInt(mbr.height)
            writeLong(entry.childOffset)
        }

        override fun read(kryo: Kryo, input: Input, type: Class<Entry>): Entry = with(input) {
            val x = readInt()
            val y = readInt()
            val width = readInt()
            val height = readInt()
            val childOffset = readLong()
            Entry(storage, Rectangle(x, y, width, height), childOffset)
        }
    }

    companion object {
        const val SIZE = 24

        val LEFT_TO_RIGHT_BY_LEFT_COMPARATOR: Comparator<Entry> = Comparator.comparingInt { it.mbr.x }
        val LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR: Comparator<Entry> = Comparator.comparingInt { it.mbr.x + it.mbr.width }
        val TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR: Comparator<Entry> = Comparator.comparingInt { it.mbr.y + it.mbr.height }
        val TOP_TO_BOTTOM_BY_TOP_COMPARATOR: Comparator<Entry> = Comparator.comparingInt { it.mbr.y }

        val X_COMPARATORS: Collection<Comparator<Entry>> =
            listOf(LEFT_TO_RIGHT_BY_LEFT_COMPARATOR, LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR)
        val Y_COMPARATORS: Collection<Comparator<Entry>> =
            listOf(TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR, TOP_TO_BOTTOM_BY_TOP_COMPARATOR)

        @JvmStatic
        fun union(entries: List<Entry>): Rectangle =
            entries.asSequence().map { it.mbr }
                .reduceOrNull { acc, r -> acc.union(r) } ?: Rectangle()

        @JvmStatic
        fun margin(entries: List<Entry>): Int =
            entries.asSequence().map { margin(it.mbr) }.sum()
    }
}
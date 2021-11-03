package lva.spatialindex.index

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

    override fun equals(other: Any?) =
        if (other is Entry) body == other.body else false

    override fun hashCode() =
        body.hashCode()

    companion object {
        const val SIZE = 24
    }
}

internal fun List<Entry>.union(): Rectangle =
    asSequence().map { it.mbr }
        .reduceOrNull { acc, r -> acc.union(r) } ?: Rectangle()

internal fun List<Entry>.margin(): Int =
    sumOf { it.mbr.margin() }

package lva.spatialindex.index

import arrow.core.Either
import lva.spatialindex.storage.Storage
import java.awt.Rectangle


/**
 * @author vlitvinenko
 */
internal class Entry(private val storage: Storage<Node>, mbr: Rectangle, childOffset: Long) {
    private data class Body(var mbr: Rectangle, val childOffset: Long)
    private val body = Body(mbr, childOffset)

    var mbr by body::mbr
    val childOffset by body::childOffset
    val childNode get() = data().swap().orNull()
    val isLeaf get() = childOffset < 0

    fun data(): Either<Node, Long> = if (childOffset >= 0)
        Either.Left(storage.read(childOffset)) else Either.Right(childOffset)

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
    sumOf { it.mbr.margin }

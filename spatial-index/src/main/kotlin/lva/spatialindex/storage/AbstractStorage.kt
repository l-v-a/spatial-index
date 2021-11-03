package lva.spatialindex.storage

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * @author vlitvinenko
 */
abstract class AbstractStorage<T : Any>(private val storageSpace: StorageSpace, private val recordSize: Int) : Storage<T> {
    interface Serializer<T : Any> {
        fun serialize(t: T): ByteArray
        fun deserialize(bytes: ByteArray): T
    }

    abstract class AbstractSerializer<T : Any> : Serializer<T> {
        override fun serialize(t: T): ByteArray =
            ByteArrayOutputStream().use {
                serializeTo(it, t)
                it.toByteArray()
            }

        override fun deserialize(bytes: ByteArray): T =
            ByteArrayInputStream(bytes).use { deserializeFrom(it) }

        abstract fun serializeTo(outputStream: OutputStream, t: T)
        abstract fun deserializeFrom(inputStream: InputStream): T
    }

    abstract val serializer: Serializer<T>

    override fun add(t: T): Long {
        val bytes = toBytes(t)
        return storageSpace.allocate(roundToRecordSize(bytes.size).toLong())
            .also { storageSpace.writeBytes(it, bytes) }
    }

    override fun write(offset: Long, t: T) {
        check(offset >= 0) { "out of bounds. Offset: $offset" }
        storageSpace.writeBytes(offset, toBytes(t))
    }

    override fun read(offset: Long): T {
        val bytes = storageSpace.readBytes(offset, recordSize)
        return serializer.deserialize(bytes)
    }

    private fun toBytes(t: T): ByteArray = serializer.serialize(t).also {
        check(it.size <= recordSize) { "record max size exceeds. Max size: $recordSize" }
    }

    override fun clear() =
        storageSpace.clear()

    private fun roundToRecordSize(size: Int): Int =
        (size + (recordSize - 1)) and -recordSize
}
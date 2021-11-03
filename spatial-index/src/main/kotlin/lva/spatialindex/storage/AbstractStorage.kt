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
                write(it, t)
                it.toByteArray()
            }

        override fun deserialize(bytes: ByteArray): T =
            ByteArrayInputStream(bytes).use { read(it) }

        abstract fun write(outputStream: OutputStream, t: T)
        abstract fun read(inputStream: InputStream): T
    }

    abstract val serializer: Serializer<T>

    override fun add(t: T): Long {
        val buff = serializer.serialize(t)
        check(buff.size <= recordSize) { "record max size exceeds" }

        val offset = storageSpace.allocate(roundToRecordSize(buff.size).toLong())
        storageSpace.writeBytes(offset, buff)
        return offset
    }

    override fun write(offset: Long, t: T) {
        val buff = serializer.serialize(t)
        check(buff.size <= recordSize)  { "record max size exceeds" }
        check(offset >= 0) { "out of bounds" }

        storageSpace.writeBytes(offset, buff)
    }

    override fun read(offset: Long): T {
        val buff = storageSpace.readBytes(offset, recordSize)
        return serializer.deserialize(buff)
    }

    override fun clear() =
        storageSpace.clear()

    private fun roundToRecordSize(size: Int): Int =
        size + (recordSize - 1) and -recordSize
}
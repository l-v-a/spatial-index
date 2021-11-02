package lva.spatialindex.memory

import lva.spatialindex.storage.StorageSpace
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode

/**
 * @author vlitvinenko
 */
internal class Segment(val filePath: String,  capacity: Long) : StorageSpace {
    private val dataTLS: ThreadLocal<ByteBuffer>
    private var _size = 0L
    private var _capacity = capacity

    init {
        _capacity = roundToPage(_capacity)
        val data = mapBackingFile(filePath, this.capacity)
        dataTLS = ThreadLocal.withInitial { data.duplicate() }
    }

    override val size: Long
        get() = _size

    override val capacity: Long
        get() = _capacity

    override fun readBytes(pos: Long, size: Int): ByteArray =
        ByteArray(size).also { readBytes(pos, it) }

    override fun readBytes(pos: Long, buff: ByteArray) {
        dataTLS.get()
            .position(pos.toInt()).get(buff)
    }

    override fun writeBytes(pos: Long, buff: ByteArray) {
        dataTLS.get()
            .position(pos.toInt()).put(buff)
    }

    override fun allocate(sizeOf: Long): Long {
        check(size + sizeOf <= capacity) {
            "Out of segment space. capacity: $capacity, size: $size, sizeOf: $sizeOf"
        }

        val offset = _size
        _size += sizeOf
        return offset
    }

    override fun clear() {
        _size = 0
    }

    companion object {
        private const val PAGE_SIZE = 4096

        private fun mapBackingFile(filePath: String, capacity: Long): ByteBuffer =
            RandomAccessFile(filePath, "rw").use { backingFile ->
                backingFile.setLength(capacity)
                backingFile.channel.use {
                    it.map(MapMode.READ_WRITE, 0L, capacity)
                }
            }

        private fun roundToPage(i: Long): Long =
            i + (PAGE_SIZE - 1) and (-PAGE_SIZE).toLong()

    }
}
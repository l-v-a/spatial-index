package lva.spatialindex.memory

import lva.spatialindex.storage.StorageSpace
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Path

/**
 * @author vlitvinenko
 */
internal class Segment(val filePath: Path, capacity: Long) : StorageSpace {
    private val dataTLS: ThreadLocal<ByteBuffer>
    val capacity: Long = roundToPage(capacity)
    var size = 0L
        private set

    init {
        val data = mapBackingFile(filePath, this.capacity)
        dataTLS = ThreadLocal.withInitial { data.duplicate() }
    }

    override fun readBytes(pos: Long, size: Int): ByteArray =
        ByteArray(size).also { readBytes(pos, it) }

    override fun readBytes(pos: Long, buff: ByteArray) {
        dataTLS.get()
            .position(pos.toInt()).get(buff)
    }

    override fun writeBytes(pos: Long, data: ByteArray) {
        dataTLS.get()
            .position(pos.toInt()).put(data)
    }

    override fun allocate(sizeOf: Long): Long {
        check(size + sizeOf <= capacity) {
            "Out of segment space. capacity: $capacity, size: $size, sizeOf: $sizeOf"
        }

        val offset = size
        size += sizeOf
        return offset
    }

    override fun clear() {
        size = 0
    }

    companion object {
        private const val PAGE_SIZE = 4096L

        private fun mapBackingFile(filePath: Path, capacity: Long): ByteBuffer =
            RandomAccessFile(filePath.toString(), "rw").use { backingFile ->
                backingFile.setLength(capacity)
                backingFile.channel.use {
                    it.map(MapMode.READ_WRITE, 0L, capacity)
                }
            }

        private fun roundToPage(i: Long): Long =
            (i + (PAGE_SIZE - 1)) and (-PAGE_SIZE)

    }
}
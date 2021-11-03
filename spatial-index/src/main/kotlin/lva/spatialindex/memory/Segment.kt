package lva.spatialindex.memory

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Path

/**
 * @author vlitvinenko
 */
internal class Segment(val filePath: Path, capacity: Int) {
    private val dataTLS: ThreadLocal<ByteBuffer>
    val capacity: Int = roundToPage(capacity)
    var size: Int = 0
        private set

    init {
        val data = mapBackingFile(filePath, this.capacity.toLong())
        dataTLS = ThreadLocal.withInitial { data.duplicate() }
    }

    fun readBytes(pos: Int, bytes: ByteArray) {
        dataTLS.get().position(pos).get(bytes)
    }

    fun writeBytes(pos: Int, bytes: ByteArray) {
        dataTLS.get().position(pos).put(bytes)
    }

    fun allocate(sizeOf: Int): Int {
        check(size + sizeOf <= capacity) {
            "Out of segment space. capacity: $capacity, size: $size, sizeOf: $sizeOf"
        }

        val offset = size
        size += sizeOf
        return offset
    }

    fun clear() {
        size = 0
    }

    companion object {
        private const val PAGE_SIZE = 4096

        private fun mapBackingFile(filePath: Path, capacity: Long): ByteBuffer =
            RandomAccessFile(filePath.toString(), "rw").use { backingFile ->
                backingFile.setLength(capacity)
                backingFile.channel.use {
                    it.map(MapMode.READ_WRITE, 0L, capacity)
                }
            }

        private fun roundToPage(i: Int): Int =
            (i + (PAGE_SIZE - 1)) and (-PAGE_SIZE)

    }
}
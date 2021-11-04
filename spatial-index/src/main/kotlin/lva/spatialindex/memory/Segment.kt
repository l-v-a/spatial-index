package lva.spatialindex.memory

import lva.spatialindex.memory.SegmentStorageSpace.Companion.safeDelete
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Path

/**
 * @author vlitvinenko
 */
internal class Segment(private val filePath: Path, capacity: Int) {
    private val mappedBuffer: ThreadLocal<ByteBuffer>
    val capacity: Int = roundToPage(capacity)
    var size: Int = 0
        private set

    init {
        val data = mapBackingFile(filePath, this.capacity.toLong())
        mappedBuffer = ThreadLocal.withInitial { data.duplicate() }
    }

    fun readBytes(pos: Int, bytes: ByteArray) {
        mappedBuffer.get().position(pos).get(bytes)
    }

    fun writeBytes(pos: Int, bytes: ByteArray) {
        mappedBuffer.get().position(pos).put(bytes)
    }

    fun allocate(sizeOf: Int): Int {
        check(size + sizeOf <= capacity) {
            "Out of segment space. capacity: $capacity, size: $size, sizeOf: $sizeOf"
        }
        return size.also { size += sizeOf }
    }

    fun remove() =
        filePath.safeDelete()

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
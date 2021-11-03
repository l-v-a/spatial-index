package lva.spatialindex.memory

import lva.spatialindex.storage.StorageSpace
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author vlitvinenko
 */
class SegmentStorageSpace(segmentsRoot: String, private val segmentCapacity: Int) : StorageSpace {
    private val segmentsRoot = Path.of(segmentsRoot)
    private val segments = mutableListOf<Segment>()

    init {
        Files.createDirectories(this.segmentsRoot)
    }

    override fun readBytes(pos: Long, size: Int): ByteArray = ByteArray(size).also {
        segments[segnum(pos)].readBytes(offset(pos), it)
    }

    override fun writeBytes(pos: Long, bytes: ByteArray) =
        segments[segnum(pos)].writeBytes(offset(pos), bytes)

    override fun allocate(sizeOf: Int): Long {
        check(sizeOf <= segmentCapacity) {
            "Unable to allocate more than segment size. Segment size: $segmentCapacity, sizeOf: $sizeOf"
        }

        fun Segment.hasEnoughFreeSpace(sizeOf: Int) =
            size + sizeOf <= capacity

        val segment = segments.lastOrNull()?.takeIf {
            it.hasEnoughFreeSpace(sizeOf)
        } ?: run {
            val segmentFilePath = segmentsRoot.resolve("segment_${segments.size}.bin")
            Segment(segmentFilePath, segmentCapacity).also { segments += it }
        }

        val offset = segment.allocate(sizeOf)
        return position(segments.size - 1, offset)
    }

    override fun clear() {
        segments.forEach { it.remove() }
        segments.clear()

        segmentsRoot.safeDelete()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SegmentStorageSpace::class.java)

        private fun position(segment: Int, offset: Int): Long =
            (segment.toLong() shl 32) or (0xFFFF_FFFFL and offset.toLong())

        private fun segnum(pos: Long): Int =
            (pos ushr 32).toInt()

        private fun offset(pos: Long): Int =
            (0xFFFF_FFFFL and pos).toInt()

        fun Path.safeDelete() {
            try {
                Files.deleteIfExists(this)
            } catch (e: Exception) {
                log.warn("Unable to delete file $this", e)
            }
        }
    }
}
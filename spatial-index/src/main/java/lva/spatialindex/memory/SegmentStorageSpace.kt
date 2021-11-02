package lva.spatialindex.memory

import lva.spatialindex.storage.StorageSpace
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author vlitvinenko
 */
class SegmentStorageSpace(segmentsRoot: String, private val segmentSize: Long) : StorageSpace {
    private val segmentsRoot = Path.of(segmentsRoot)
    private val segments = mutableListOf<Segment>()

    init {
        Files.createDirectories(this.segmentsRoot)
    }

    override fun readBytes(pos: Long, size: Int): ByteArray =
        segments[segnum(pos)].readBytes(offset(pos), size)

    override fun readBytes(pos: Long, buff: ByteArray) =
        segments[segnum(pos)].readBytes(offset(pos), buff)

    override fun writeBytes(pos: Long, data: ByteArray) =
        segments[segnum(pos)].writeBytes(offset(pos), data)

    override fun allocate(sizeOf: Long): Long {
        check(sizeOf <= segmentSize) {
            "Unable to allocate more than segment size. Segment size: $segmentSize, sizeOf: $sizeOf"
        }

        val segment = segments.lastOrNull()?.takeIf {
            it.size + sizeOf <= it.capacity
        } ?: run {
            val segmentFileName = segmentsRoot.resolve("segment_${segments.size}.bin")
            Segment(segmentFileName.toString(), segmentSize).also { segments += it }
        }

        val offset = segment.allocate(sizeOf)
        return position(segments.size - 1, offset)
    }

    override fun clear() {
        segments.forEach { segment ->
            segment.clear()
            Path.of(segment.filePath).safeDelete()
        }

        segments.clear()
        segmentsRoot.safeDelete()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SegmentStorageSpace::class.java)

        private fun position(segment: Int, offset: Long): Long =
            segment.toLong() shl 32 or (0xFFFF_FFFFL and offset)

        private fun segnum(pos: Long): Int =
            (pos ushr 32).toInt()

        private fun offset(pos: Long): Long =
            0xFFFF_FFFFL and pos

        private fun Path.safeDelete() {
            try {
                Files.deleteIfExists(this)
            } catch (e: Exception) {
                log.warn("Unable to delete file $this", e)
            }
        }
    }
}
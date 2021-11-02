package lva.spatialindex.storage

/**
 * @author vlitvinenko
 */
interface StorageSpace {
    fun allocate(sizeOf: Long): Long
    fun readBytes(pos: Long, size: Int): ByteArray
    fun readBytes(pos: Long, buff: ByteArray)
    fun writeBytes(pos: Long, data: ByteArray)
    fun clear()
}
package lva.spatialindex.storage

/**
 * @author vlitvinenko
 */
interface StorageSpace {
    val size: Long
    val capacity: Long
    fun readBytes(pos: Long, size: Int): ByteArray
    fun readBytes(pos: Long, buff: ByteArray)
    fun writeBytes(pos: Long, data: ByteArray)
    fun allocate(sizeOf: Long): Long
    fun clear()
}
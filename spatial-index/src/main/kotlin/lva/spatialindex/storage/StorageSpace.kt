package lva.spatialindex.storage

/**
 * @author vlitvinenko
 */
interface StorageSpace {
    fun allocate(sizeOf: Int): Long
    fun readBytes(pos: Long, size: Int): ByteArray
    fun readBytes(pos: Long, bytes: ByteArray)
    fun writeBytes(pos: Long, bytes: ByteArray)
    fun clear()
}
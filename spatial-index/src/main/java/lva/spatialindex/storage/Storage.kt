package lva.spatialindex.storage

/**
 * @author vlitvinenko
 */
interface Storage<T : Any> {
    fun add(t: T): Long
    fun write(offset: Long, t: T)
    fun read(offset: Long): T
    fun clear()
}
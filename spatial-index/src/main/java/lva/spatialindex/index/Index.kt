package lva.spatialindex.index

import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
interface Index : AutoCloseable {
    fun search(area: Rectangle): Collection<Long>
    override fun close()
}
package lva.spatialindex.index

import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
interface Index {
    fun search(area: Rectangle): Collection<Long>
}
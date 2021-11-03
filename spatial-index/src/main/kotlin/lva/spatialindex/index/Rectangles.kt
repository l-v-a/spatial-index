package lva.spatialindex.index

import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
fun Rectangle.area(): Long =
    (width * height).toLong()

fun Rectangle.margin(): Int =
    2 * (height + width)


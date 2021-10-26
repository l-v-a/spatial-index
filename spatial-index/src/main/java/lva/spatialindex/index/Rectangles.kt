package lva.spatialindex.index

import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
fun area(r: Rectangle): Long = (r.width * r.height).toLong()

fun margin(r: Rectangle): Int = 2 * (r.height + r.width)


package lva.spatialindex.index

import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author vlitvinenko
 */
class RectanglesTest {
    @Test
    fun test_area() {
        assertEquals(0, Rectangle(1, 2, 10, 0).area)
        assertEquals(12, Rectangle(1, 2, 3, 4).area)
    }

    @Test
    fun test_margin() {
        assertEquals(0, Rectangle(1, 2, 0, 0).margin)
        assertEquals(14, Rectangle(1, 2, 3, 4).margin)
    }
}
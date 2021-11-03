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
        assertEquals(0, Rectangle().area())
        assertEquals(8, Rectangle(0, 0, 2, 4).area())
    }

    @Test
    fun test_margin() {
        assertEquals(0, Rectangle().margin())
        assertEquals(12, Rectangle(0, 0, 2, 4).margin())
    }
}
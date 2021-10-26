package lva.spatialindex.index;

import org.junit.Test;

import java.awt.Rectangle;

import static lva.spatialindex.index.RectanglesKt.area;
import static lva.spatialindex.index.RectanglesKt.margin;
import static org.junit.Assert.assertEquals;

/**
 * @author vlitvinenko
 */
public class RectanglesTest {
    @Test
    public void test_area() {
        assertEquals(0, area(new Rectangle()));
        assertEquals(8, area(new Rectangle(0, 0, 2, 4)));
    }

    @Test
    public void test_margin() {
        assertEquals(0, margin(new Rectangle()));
        assertEquals(12, margin(new Rectangle(0, 0, 2, 4)));
    }
}
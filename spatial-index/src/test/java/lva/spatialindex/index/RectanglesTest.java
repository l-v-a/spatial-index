package lva.spatialindex.index;

import org.junit.Test;

import java.awt.Rectangle;

import static org.junit.Assert.assertEquals;

/**
 * @author vlitvinenko
 */
public class RectanglesTest {
    @Test
    public void test_area() {
        assertEquals(0, Rectangles.area(new Rectangle()));
        assertEquals(8, Rectangles.area(new Rectangle(0, 0, 2, 4)));
    }

    @Test
    public void test_margin() {
        assertEquals(0, Rectangles.margin(new Rectangle()));
        assertEquals(12, Rectangles.margin(new Rectangle(0, 0, 2, 4)));
    }
}
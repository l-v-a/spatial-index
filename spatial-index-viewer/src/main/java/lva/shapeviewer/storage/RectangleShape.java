package lva.shapeviewer.storage;

import lombok.NonNull;

import java.awt.*;

/**
 * @author vlitvinenko
 */
public class RectangleShape extends AbstractShape {
    private final Rectangle rectangle;

    public RectangleShape(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }

    public RectangleShape(@NonNull Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    private RectangleShape() { this(0, 0, 0, 0); } // for deserialization

    @Override
    public Rectangle getMbr() {
        return new Rectangle(rectangle);
    }

    @Override
    public void draw(@NonNull Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        g.setColor(isActive ? Color.RED: Color.BLACK);
        g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public boolean hitTest(int x, int y) {
        return rectangle.contains(x, y);
    }
}

package lva.spatialindex.viewer.storage;

import java.awt.*;

public class CircleShape extends AbstractShape {
    private final int x;
    private final int y;
    private final int radius;

    public CircleShape(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    private CircleShape() { this(0, 0, 0); } // for deserialization

    @Override
    public Rectangle getMbr() {
        return new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void draw(Graphics g) {
        Rectangle boundRect = getMbr();

        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height);

        g.setColor(isActive ? Color.RED: Color.BLACK);
        g.drawOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height);
    }

    @Override
    public boolean hitTest(int x, int y) {
        return (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) <= radius * radius;
    }
}

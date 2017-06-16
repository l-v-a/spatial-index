package lva.shapeviewer;

import lva.spatialindex.Exceptions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author vlitvinenko
 */
public class Shape {
    // TODO: make interface

    private final Rectangle mbr;
    private boolean isActive;
    private int order;
    private long offset;

    public Shape(Rectangle mbr) {
        this.mbr = mbr;
    }

    public Rectangle getMbr() {
        return mbr;
    }

    public void draw(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(mbr.x, mbr.y, mbr.width, mbr.height);

        g.setColor(isActive ? Color.RED: Color.BLACK);
        g.drawRect(mbr.x, mbr.y, mbr.width, mbr.height);
    }

    // TODO: move to ans use reflection
    static byte[] serialize(Shape shape) {
        return Exceptions.toRuntime(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(baos);

            os.writeInt(shape.mbr.x);
            os.writeInt(shape.mbr.y);
            os.writeInt(shape.mbr.width);
            os.writeInt(shape.mbr.height);
            os.writeInt(shape.order);
            os.writeBoolean(shape.isActive);

            return baos.toByteArray();
        });
    }

    static Shape deserialize(byte[] buff) {
        return Exceptions.toRuntime(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(buff);
            DataInputStream is = new DataInputStream(bais);

            int x = is.readInt();
            int y = is.readInt();
            int width = is.readInt();
            int height = is.readInt();
            int order = is.readInt();
            boolean isActive = is.readBoolean();

            Shape shape = new Shape(new Rectangle(x, y, width, height));
            shape.setOrder(order);
            shape.setActive(isActive);

            return shape;
        });
    }

    public boolean hitTest(int x, int y) {
        return getMbr().contains(x, y);
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

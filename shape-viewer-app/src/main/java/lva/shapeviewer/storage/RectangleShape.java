package lva.shapeviewer.storage;

import lombok.NonNull;
import lva.spatialindex.utils.Exceptions;

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
public class RectangleShape extends AbstractShape {
    private final Rectangle rectangle;

    public RectangleShape(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }

    public RectangleShape(@NonNull Rectangle rectangle) {
        this.rectangle = rectangle;
    }

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

    // TODO: use reflection
    @NonNull
    static byte[] serialize(@NonNull Shape shape) {
        RectangleShape rect = (RectangleShape) shape;
        return Exceptions.toRuntime(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream os = new DataOutputStream(baos);

            os.writeInt(rect.rectangle.x);
            os.writeInt(rect.rectangle.y);
            os.writeInt(rect.rectangle.width);
            os.writeInt(rect.rectangle.height);
            os.writeInt(rect.order);
            os.writeBoolean(rect.isActive);

            return baos.toByteArray();
        });
    }

    @NonNull
    static Shape deserialize(@NonNull byte[] buff) {
        return Exceptions.toRuntime(() -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(buff);
            DataInputStream is = new DataInputStream(bais);

            int x = is.readInt();
            int y = is.readInt();
            int width = is.readInt();
            int height = is.readInt();
            int order = is.readInt();
            boolean isActive = is.readBoolean();

            RectangleShape rect = new RectangleShape(new Rectangle(x, y, width, height));
            rect.setOrder(order);
            rect.setActive(isActive);

            return rect;
        });
    }

    public boolean hitTest(int x, int y) {
        return rectangle.contains(x, y);
    }
}

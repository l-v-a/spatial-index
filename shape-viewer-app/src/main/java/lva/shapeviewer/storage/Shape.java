package lva.shapeviewer.storage;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
public class Shape {
    // TODO: make interface

    @NonNull
    @Getter
    private final Rectangle mbr;

    @Getter
    @Setter
    private boolean isActive;

    @Getter
    @Setter
    private int order;

    @Getter
    @Setter
    private long offset;

    public Shape(int x, int y, int width, int height) {
        this(new Rectangle(x, y, width, height));
    }

    public Shape(@NonNull Rectangle mbr) {
        this.mbr = mbr;
    }

    public void draw(@NonNull Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(mbr.x, mbr.y, mbr.width, mbr.height);

        g.setColor(isActive ? Color.RED: Color.BLACK);
        g.drawRect(mbr.x, mbr.y, mbr.width, mbr.height);
    }

    // TODO: move to ans use reflection
    @NonNull
    static byte[] serialize(@NonNull Shape shape) {
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

            Shape shape = new Shape(new Rectangle(x, y, width, height));
            shape.setOrder(order);
            shape.setActive(isActive);

            return shape;
        });
    }

    public boolean hitTest(int x, int y) {
        return getMbr().contains(x, y);
    }
}

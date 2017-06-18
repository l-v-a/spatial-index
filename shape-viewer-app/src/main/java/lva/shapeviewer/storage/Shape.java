package lva.shapeviewer.storage;

import lombok.NonNull;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * @author vlitvinenko
 */
public interface Shape {
    Rectangle getMbr();

    boolean isActive();

    void setActive(boolean isActive);

    int getOrder();

    void setOrder(int order);

    long getOffset();

    void setOffset(long offset);

    void draw(@NonNull Graphics g);

    boolean hitTest(int x, int y);

    static byte[] serialize(@NonNull Shape shape) {
        // TODO: reimplement
        return RectangleShape.serialize(shape);
    }

    static Shape deserialize(@NonNull byte[] buff) {
        // TODO: reimplement
        return RectangleShape.deserialize(buff);
    }
}

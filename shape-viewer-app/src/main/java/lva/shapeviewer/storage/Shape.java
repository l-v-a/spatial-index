package lva.shapeviewer.storage;

import lombok.NonNull;

import java.awt.*;

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
}

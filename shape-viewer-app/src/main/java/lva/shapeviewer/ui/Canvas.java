package lva.shapeviewer.ui;

import lva.shapeviewer.Shape;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;


class Canvas extends JComponent {
    private Rectangle viewport = new Rectangle();
    private Collection<Shape> shapes = new ArrayList<>();

    @Override
    public void paint(Graphics g) {
        g.translate(-viewport.x, -viewport.y);
        for (Shape shape : shapes) {
            shape.draw(g);
        }
    }

    void setViewport(Rectangle viewport) {
        this.viewport = viewport;
    }

    void setShapes(Collection<Shape> shapes) {
        this.shapes = shapes;
    }
}




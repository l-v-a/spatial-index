package lva.shapeviewer.ui;

import lva.shapeviewer.Shape;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

//class Model {
//    private final DbStorage dbStorage;
//    private final Index index;
//
//}


class Canvas extends JComponent {
    private Rectangle viewport = new Rectangle();
    private List<Shape> shapes = new ArrayList<>();

    Canvas() {

    }

    @Override
    public void paint(Graphics g) {
        g.translate(-viewport.x, -viewport.y);
        for (Shape shape: shapes) {
            shape.draw(g);
        }

    }

    public void setViewport(Rectangle viewport) {
        this.viewport = viewport;
    }

    public void setShapes(List<Shape> shapes) {
        this.shapes = shapes;
    }
}



